package com.osei.aimusicstudio.repository

import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.local.repository.LocalSongStorage
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import com.osei.aimusicstudio.data.remote.service.DisabledMusicGenerationService
import com.osei.aimusicstudio.data.remote.validation.AudioReferenceValidator
import com.osei.aimusicstudio.data.repository.OfflineFirstSongRepository
import com.osei.aimusicstudio.data.repository.SongRepository
import com.osei.aimusicstudio.data.sync.StudioSyncCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineFirstSafetyTest {

    private lateinit var fakeLocalStorage: FakeLocalSongStorage
    private lateinit var mockRemoteRepo: MockRemoteSongRepository
    private lateinit var coordinator: StudioSyncCoordinator
    private lateinit var offlineFirstRepo: OfflineFirstSongRepository

    @Before
    fun setUp() {
        fakeLocalStorage = FakeLocalSongStorage()
        mockRemoteRepo = MockRemoteSongRepository()
        coordinator = StudioSyncCoordinator(
            localSongStorage = fakeLocalStorage,
            localUserStorage = FakeLocalUserStorage(),
            remoteSongRepository = mockRemoteRepo,
            remoteUserRepository = null,
            isCloudSyncEnabled = false // Default: Local-Only Mode
        )
        offlineFirstRepo = OfflineFirstSongRepository(
            localSongStorage = fakeLocalStorage,
            syncCoordinator = coordinator
        )
    }

    @Test
    fun cloudSync_defaultsToOff() {
        assertTrue(offlineFirstRepo.isLocalOnly)
        assertFalse(coordinator.isCloudSyncEnabled)
        assertFalse(coordinator.isSyncEnabled)
    }

    @Test
    fun localOnlyMode_createsAndPersistsDraftLocallyWithoutRemoteCalls() = runBlocking {
        val draft = Song(
            id = "song_local_1",
            userId = "user_123",
            title = "Accra Midnight Highlife",
            lyrics = "Ghana mma, mo mbra fie",
            status = "draft",
            audioUrl = "",
            phaseVersion = "phase_3_2_offline_first"
        )

        val result = offlineFirstRepo.createSong(draft)
        assertTrue(result.isSuccess)
        assertEquals("song_local_1", result.getOrNull())

        val retrieved = offlineFirstRepo.getSong("user_123", "song_local_1")
        assertNotNull(retrieved)
        assertEquals("Accra Midnight Highlife", retrieved?.title)
        assertEquals("draft", retrieved?.status)
        assertTrue(retrieved?.audioUrl.isNullOrEmpty())
        assertEquals(0, mockRemoteRepo.createdSongs.size)
    }

    @Test
    fun localOnlyMode_updatesDraftLocallyWithoutRemoteCalls() = runBlocking {
        val draft = Song(
            id = "song_local_2",
            userId = "user_123",
            title = "Kumasi Vibes",
            status = "draft"
        )
        offlineFirstRepo.createSong(draft)

        val updated = draft.copy(title = "Kumasi Vibes (Acoustic Edit)")
        val updateResult = offlineFirstRepo.updateSong(updated)
        assertTrue(updateResult.isSuccess)

        val retrieved = offlineFirstRepo.getSong("user_123", "song_local_2")
        assertEquals("Kumasi Vibes (Acoustic Edit)", retrieved?.title)
        assertEquals(0, mockRemoteRepo.updatedSongs.size)
    }

    @Test
    fun localDrafts_surviveRepositoryReloads() = runBlocking {
        val draft = Song(
            id = "song_persisted",
            userId = "user_123",
            title = "Cape Coast Melody",
            status = "draft"
        )
        offlineFirstRepo.createSong(draft)

        // Simulate app restart by instantiating a new repository reading from the same storage
        val reloadedRepo = OfflineFirstSongRepository(
            localSongStorage = fakeLocalStorage,
            syncCoordinator = coordinator
        )

        val retrieved = reloadedRepo.getSong("user_123", "song_persisted")
        assertNotNull(retrieved)
        assertEquals("Cape Coast Melody", retrieved?.title)
        assertEquals("draft", retrieved?.status)
    }

    @Test
    fun drafts_areExcludedFromCloudUpload() = runBlocking {
        // Explicitly enable cloud sync
        coordinator.isCloudSyncEnabled = true
        assertFalse(offlineFirstRepo.isLocalOnly)
        assertTrue(coordinator.isSyncEnabled)

        val draft = Song(
            id = "draft_local_only",
            userId = "user_123",
            title = "Highlife Secret Rhythms",
            status = "draft",
            audioUrl = ""
        )
        offlineFirstRepo.createSong(draft)

        // Trigger manual sync
        val syncResult = coordinator.syncSongs("user_123")
        assertTrue(syncResult.isSuccess)

        // The draft must be strictly excluded from remote Firestore writes
        assertEquals(0, mockRemoteRepo.createdSongs.size)
        // Local copy in Room must remain intact as draft
        val localDraft = fakeLocalStorage.getSong("user_123", "draft_local_only")
        assertNotNull(localDraft)
        assertEquals("draft", localDraft?.status)
    }

    @Test
    fun enablingCloudSync_neverConvertsDraftsIntoCompletedSongs() = runBlocking {
        val draft = Song(
            id = "draft_conversion_test",
            userId = "user_123",
            title = "Osu Acoustic Story",
            status = "draft",
            audioUrl = ""
        )
        offlineFirstRepo.createSong(draft)

        // Enable cloud sync and execute sync
        coordinator.isCloudSyncEnabled = true
        coordinator.syncSongs("user_123")

        val stored = offlineFirstRepo.getSong("user_123", "draft_conversion_test")
        assertNotNull(stored)
        assertEquals("draft", stored?.status)
        assertTrue(stored?.audioUrl.isNullOrEmpty())
    }

    @Test
    fun disabledGeneration_makesNoProviderOrHttpRequest() = runBlocking {
        val service = DisabledMusicGenerationService()
        assertEquals("disabled", service.generationMode)
        assertFalse(service.isConfigured)

        val request = MusicGenerationRequest(
            songId = "song_req_1",
            userId = "user_123",
            title = "Test Song",
            genre = "Ghanaian Highlife",
            mood = "Chill",
            vocalPreference = "Instrumental",
            language = "English"
        )

        val submitResult = service.submitGenerationJob(request, userToken = "")
        assertTrue(submitResult.isFailure)
        assertTrue(submitResult.exceptionOrNull()?.message?.contains("disabled") == true)

        val pollResult = service.pollJobStatus("job_123", userToken = "")
        assertTrue(pollResult.isFailure)
    }

    @Test
    fun remoteFailures_doNotDeleteLocalDrafts() = runBlocking {
        mockRemoteRepo.shouldFail = true
        coordinator.isCloudSyncEnabled = true

        val draft = Song(
            id = "resilient_draft",
            userId = "user_123",
            title = "Resilient Ghanaian Groove",
            status = "draft"
        )
        offlineFirstRepo.createSong(draft)

        val syncResult = coordinator.syncSongs("user_123")
        // Even if remote sync fails, local draft is never deleted or corrupted
        val localSong = offlineFirstRepo.getSong("user_123", "resilient_draft")
        assertNotNull(localSong)
        assertEquals("Resilient Ghanaian Groove", localSong?.title)
        assertEquals("draft", localSong?.status)
    }

    @Test
    fun audioReferenceValidator_strictlyRejectsDraftsAndEmptyUrls() {
        val emptyResult = AudioReferenceValidator.validate("")
        assertTrue(emptyResult is AudioReferenceValidator.ValidationResult.Invalid)

        val nullResult = AudioReferenceValidator.validate(null)
        assertTrue(nullResult is AudioReferenceValidator.ValidationResult.Invalid)

        val fakeKeywordResult = AudioReferenceValidator.validate("https://example.com/rain_sound.mp3")
        assertTrue(fakeKeywordResult is AudioReferenceValidator.ValidationResult.Invalid)
    }
}

private class MockRemoteSongRepository : SongRepository {
    val createdSongs = mutableListOf<Song>()
    val updatedSongs = mutableListOf<Song>()
    var shouldFail = false

    override fun observeSongs(userId: String): Flow<List<Song>> = flowOf(emptyList())

    override suspend fun getSong(userId: String, songId: String): Song? = null

    override suspend fun createSong(song: Song): Result<String> {
        if (shouldFail) return Result.failure(RuntimeException("Simulated network outage"))
        createdSongs.add(song)
        return Result.success(song.id)
    }

    override suspend fun updateSong(song: Song): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Simulated network outage"))
        updatedSongs.add(song)
        return Result.success(Unit)
    }

    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Simulated network outage"))
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        return Result.success(Unit)
    }
}

private class FakeLocalSongStorage : LocalSongStorage {
    val store = mutableMapOf<String, Song>()
    val pendingList = mutableListOf<LocalSongEntity>()

    override fun observeSongs(userId: String): Flow<List<Song>> = flowOf(store.values.filter { it.userId == userId })

    override suspend fun getSong(userId: String, songId: String): Song? = store[songId]?.takeIf { it.userId == userId }

    override suspend fun createSong(song: Song): Result<String> {
        store[song.id] = song
        pendingList.add(
            LocalSongEntity(
                id = song.id,
                userId = song.userId,
                title = song.title,
                description = song.description,
                genre = song.genre,
                mood = song.mood,
                vocalPreference = song.vocalPreference,
                language = song.language,
                lyrics = song.lyrics,
                isInstrumental = song.isInstrumental,
                status = song.status,
                audioUrl = song.audioUrl,
                durationSeconds = song.durationSeconds,
                isFavorite = song.isFavorite,
                phaseVersion = song.phaseVersion,
                createdAtEpoch = System.currentTimeMillis(),
                updatedAtEpoch = System.currentTimeMillis(),
                syncStatus = LocalSongEntity.SYNC_STATUS_PENDING_INSERT
            )
        )
        return Result.success(song.id)
    }

    override suspend fun updateSong(song: Song): Result<Unit> {
        store[song.id] = song
        return Result.success(Unit)
    }

    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        store.remove(songId)
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        val current = store[songId] ?: return Result.failure(NoSuchElementException())
        store[songId] = current.copy(isFavorite = !currentFavorite)
        return Result.success(Unit)
    }

    override suspend fun getPendingSyncSongs(userId: String): List<LocalSongEntity> = pendingList.filter { it.userId == userId }

    override suspend fun markAsSynced(songId: String) {
        pendingList.removeAll { it.id == songId }
    }

    override suspend fun saveRemoteSongs(songs: List<Song>) {
        songs.forEach { store[it.id] = it }
    }
}

private class FakeLocalUserStorage : com.osei.aimusicstudio.data.local.repository.LocalUserStorage {
    override fun observeUserProfile(userId: String): Flow<com.osei.aimusicstudio.data.model.UserProfile?> = flowOf(null)
    override suspend fun getUserProfile(userId: String): com.osei.aimusicstudio.data.model.UserProfile? = null
    override suspend fun createOrUpdateProfile(profile: com.osei.aimusicstudio.data.model.UserProfile): Result<Unit> = Result.success(Unit)
    override suspend fun updatePreferences(userId: String, language: String, genre: String): Result<Unit> = Result.success(Unit)
    override suspend fun getPendingSyncProfiles(): List<com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity> = emptyList()
    override suspend fun markAsSynced(userId: String) {}
    override suspend fun saveRemoteProfile(profile: com.osei.aimusicstudio.data.model.UserProfile) {}
    override suspend fun clearUserProfile(userId: String) {}
}
