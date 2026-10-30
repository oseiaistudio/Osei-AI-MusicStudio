package com.osei.aimusicstudio.remote

import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicGenerationValidationTest {

    @Test
    fun validRequest_passesValidation() {
        val request = MusicGenerationRequest(
            songId = "song_abc_123",
            userId = "user_xyz_789",
            title = "Kumasi Highlife Anthem",
            genre = "Ghanaian Highlife",
            mood = "Celebratory",
            vocalPreference = "Male",
            language = "Twi",
            lyrics = "Odo yewu, yenko fie",
            isInstrumental = false
        )

        assertTrue(request.isValid())
    }

    @Test
    fun requestWithMissingFields_failsValidation() {
        val missingSongId = MusicGenerationRequest(
            songId = "",
            userId = "user_xyz",
            title = "Title",
            genre = "Genre",
            mood = "Mood",
            vocalPreference = "Male",
            language = "English"
        )
        assertFalse(missingSongId.isValid())

        val missingUserId = MusicGenerationRequest(
            songId = "song_123",
            userId = "",
            title = "Title",
            genre = "Genre",
            mood = "Mood",
            vocalPreference = "Male",
            language = "English"
        )
        assertFalse(missingUserId.isValid())

        val missingTitle = MusicGenerationRequest(
            songId = "song_123",
            userId = "user_123",
            title = "   ",
            genre = "Genre",
            mood = "Mood",
            vocalPreference = "Male",
            language = "English"
        )
        assertFalse(missingTitle.isValid())

        val missingGenre = MusicGenerationRequest(
            songId = "song_123",
            userId = "user_123",
            title = "Valid Title",
            genre = "",
            mood = "Mood",
            vocalPreference = "Male",
            language = "English"
        )
        assertFalse(missingGenre.isValid())
    }
}
