package com.osei.aimusicstudio.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.osei.aimusicstudio.base.FirestoreEmulatorTestBase
import com.osei.aimusicstudio.data.model.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SongRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createSong_authenticatedOwner_createsDocumentAndReturnsId() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreSongRepository(firestore, auth)

        val song = Song(
            userId = aliceUid,
            title = "Kumasi Highlife Jam",
            genre = "Ghanaian Highlife",
            mood = "Uplifting",
            vocalPreference = "Male",
            language = "Twi",
            isInstrumental = false,
            status = "draft"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createSong(song) }
        assertTrue(result.isSuccess)
        val songId = result.getOrThrow()
        assertTrue(songId.isNotBlank())
    }

    @Test
    fun observeSongs_authenticatedOwner_emitsCreatedSong() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreSongRepository(firestore, auth)

        val song = Song(
            userId = aliceUid,
            title = "Accra Afrobeats Vibe",
            genre = "Afrobeats",
            mood = "Energetic",
            vocalPreference = "Female",
            language = "English",
            isInstrumental = false,
            status = "queued"
        )

        val songId = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createSong(song).getOrThrow() }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSongs(aliceUid).first { list -> list.any { it.id == songId } }
        }

        assertTrue(emitted.any { it.id == songId })
    }

    @Test
    fun observeSongs_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FirestoreSongRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSongs("fake_unauth_user").first()
            }
            fail("Expected FirebaseFirestoreException to be thrown for unauthenticated query")
        } catch (e: Throwable) {
            val rootCause = generateSequence(e) { it.cause }.firstOrNull { it is FirebaseFirestoreException }
            assertNotNull("Expected FirebaseFirestoreException in cause chain", rootCause)
            val firestoreEx = rootCause as FirebaseFirestoreException
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@oseistudio.com"
        const val BOB_EMAIL = "bob@oseistudio.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
