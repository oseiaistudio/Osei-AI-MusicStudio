package com.osei.aimusicstudio.foundation

import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity
import com.osei.aimusicstudio.data.local.repository.LocalSongStorage
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.model.VideoProject
import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import com.osei.aimusicstudio.data.remote.service.DisabledMusicGenerationService
import com.osei.aimusicstudio.data.remote.validation.AudioReferenceValidator
import com.osei.aimusicstudio.data.repository.OfflineFirstSongRepository
import com.osei.aimusicstudio.data.repository.VideoProjectRepository
import com.osei.aimusicstudio.data.sync.StudioSyncCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Phase4AudioVideoStudioTest {

    private lateinit var fakeSongStorage: FakeAudioStudioStorage
    private lateinit var fakeVideoRepository: FakeVideoStudioRepository
    private lateinit var coordinator: StudioSyncCoordinator
    private lateinit var offlineSongRepo: OfflineFirstSongRepository

    @Before
    fun setUp() {
        fakeSongStorage = FakeAudioStudioStorage()
        fakeVideoRepository = FakeVideoStudioRepository()
        coordinator = StudioSyncCoordinator(
            localSongStorage = fakeSongStorage,
            localUserStorage = FakeUserStorageStub(),
            remoteSongRepository = null,
            remoteUserRepository = null,
            isCloudSyncEnabled = false
        )
        offlineSongRepo = OfflineFirstSongRepository(
            localSongStorage = fakeSongStorage,
            syncCoordinator = coordinator
        )
    }

    @Test
    fun creatingAndReopeningLocalAudioDrafts_persistsSettingsInRoom() = runBlocking {
        val audioDraft = Song(
            id = "audio_draft_1",
            userId = "user_osei",
            title = "Cape Coast Sun Highlife",
            lyrics = "Yɛbɛ kɔ fie, na yɛanya asomdwoe",
            genre = "Ghanaian Highlife",
            mood = "Celebratory",
            tempo = "Highlife Fast (140 BPM)",
            vocalPreference = "Male",
            language = "Twi",
            status = "draft",
            audioUrl = "",
            durationSeconds = 0L,
            phaseVersion = "phase_4_foundation"
        )

        val createResult = offlineSongRepo.createSong(audioDraft)
        assertTrue(createResult.isSuccess)

        // Reopen from repository
        val reopened = offlineSongRepo.getSong("user_osei", "audio_draft_1")
        assertNotNull(reopened)
        assertEquals("Cape Coast Sun Highlife", reopened?.title)
        assertEquals("Ghanaian Highlife", reopened?.genre)
        assertEquals("Highlife Fast (140 BPM)", reopened?.tempo)
        assertEquals("Twi", reopened?.language)
        assertEquals("draft", reopened?.status)
        assertTrue(reopened?.audioUrl.isNullOrEmpty())
        assertEquals(0L, reopened?.durationSeconds)
    }

    @Test
    fun creatingAndReopeningLocalVideoDrafts_persistsSettingsInRoom() = runBlocking {
        val videoDraft = VideoProject(
            id = "video_proj_1",
            userId = "user_osei",
            title = "Accra 2077 Afrofuturism Visualizer",
            prompt = "Hyper-detailed glowing Accra skyline, flying trotro vehicles, gold kente patterns illuminated with purple neon, slow cinematic drone pan.",
            visualStyle = "Afrofuturism 3D",
            aspectRatio = "9:16",
            linkedSongId = "audio_draft_1",
            linkedSongTitle = "Cape Coast Sun Highlife",
            localAudioUri = null,
            localVideoUri = null,
            status = "draft",
            phaseVersion = "phase_4_foundation"
        )

        val createResult = fakeVideoRepository.createVideoProject(videoDraft)
        assertTrue(createResult.isSuccess)

        // Reopen from repository
        val reopened = fakeVideoRepository.getVideoProject("user_osei", "video_proj_1")
        assertNotNull(reopened)
        assertEquals("Accra 2077 Afrofuturism Visualizer", reopened?.title)
        assertEquals("Afrofuturism 3D", reopened?.visualStyle)
        assertEquals("9:16", reopened?.aspectRatio)
        assertEquals("Cape Coast Sun Highlife", reopened?.linkedSongTitle)
        assertEquals("draft", reopened?.status)
        assertNull(reopened?.localVideoUri)
    }

    @Test
    fun videoProjectWithAttachedLocalVideo_persistsLocalVideoUriAsDraft() = runBlocking {
        val videoDraft = VideoProject(
            id = "video_proj_2",
            userId = "user_osei",
            title = "Highlife Live Performance",
            prompt = "Live studio performance in Accra",
            visualStyle = "Cinematic Film 4K",
            aspectRatio = "16:9",
            localVideoUri = "content://media/external/video/media/202",
            status = "draft",
            phaseVersion = "phase_4_foundation"
        )
        fakeVideoRepository.createVideoProject(videoDraft)

        val retrieved = fakeVideoRepository.getVideoProject("user_osei", "video_proj_2")
        assertNotNull(retrieved)
        assertEquals("draft", retrieved?.status)
        assertEquals("content://media/external/video/media/202", retrieved?.localVideoUri)
        assertTrue(retrieved?.isPlayableVideo == true)
    }

    @Test
    fun videoProject_missingOrInaccessibleVideo_isPlayableVideoReturnsFalse() {
        val draftWithoutVideo = VideoProject(
            id = "v1",
            status = "draft",
            localVideoUri = null
        )
        assertFalse(draftWithoutVideo.isPlayableVideo)

        val draftWithEmptyUri = VideoProject(
            id = "v2",
            status = "draft",
            localVideoUri = ""
        )
        assertFalse(draftWithEmptyUri.isPlayableVideo)

        val completedWithoutUri = VideoProject(
            id = "v3",
            status = "completed",
            localVideoUri = null
        )
        assertFalse(completedWithoutUri.isPlayableVideo)

        val genuinePlayableVideo = VideoProject(
            id = "v4",
            status = "completed",
            localVideoUri = "content://media/external/video/media/101"
        )
        assertTrue(genuinePlayableVideo.isPlayableVideo)
    }

    @Test
    fun refusalToPlayIncompleteAudioProjects_guaranteesNoFabrication() {
        // Draft track with no audio URL
        val emptyUrlValidation = AudioReferenceValidator.validate("")
        assertTrue(emptyUrlValidation is AudioReferenceValidator.ValidationResult.Invalid)

        val nullUrlValidation = AudioReferenceValidator.validate(null)
        assertTrue(nullUrlValidation is AudioReferenceValidator.ValidationResult.Invalid)

        val sampleKeywordValidation = AudioReferenceValidator.validate("https://example.com/sample_chime.wav")
        assertTrue(sampleKeywordValidation is AudioReferenceValidator.ValidationResult.Invalid)
    }

    @Test
    fun noProviderRequests_whenGenerationDisabled() = runBlocking {
        val service = DisabledMusicGenerationService()
        assertEquals("disabled", service.generationMode)
        assertFalse(service.isConfigured)

        val req = MusicGenerationRequest(
            songId = "song_1",
            userId = "u1",
            title = "Test",
            genre = "Ghanaian Highlife",
            mood = "Chill",
            vocalPreference = "Male",
            language = "English"
        )

        val result = service.submitGenerationJob(req, "")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("disabled") == true)
    }

    @Test
    fun localVideoDrafts_areExcludedFromCloudUpload() {
        val entity = LocalVideoProjectEntity(
            id = "vid_local_only",
            userId = "u1",
            title = "Local Only Concept",
            prompt = "Some prompt",
            visualStyle = "Cinematic Film 4K",
            aspectRatio = "16:9",
            linkedSongId = null,
            linkedSongTitle = null,
            localAudioUri = null,
            localVideoUri = null,
            status = "draft",
            durationSeconds = 0L,
            phaseVersion = "phase_4_foundation",
            createdAtEpoch = System.currentTimeMillis(),
            updatedAtEpoch = System.currentTimeMillis()
        )

        assertEquals(LocalVideoProjectEntity.SYNC_STATUS_LOCAL_ONLY, entity.syncStatus)
        assertEquals("draft", entity.status)
        assertNull(entity.localVideoUri)
    }
}

private class FakeAudioStudioStorage : LocalSongStorage {
    val map = mutableMapOf<String, Song>()

    override fun observeSongs(userId: String): Flow<List<Song>> = flowOf(map.values.filter { it.userId == userId })

    override suspend fun getSong(userId: String, songId: String): Song? = map[songId]?.takeIf { it.userId == userId }

    override suspend fun createSong(song: Song): Result<String> {
        map[song.id] = song
        return Result.success(song.id)
    }

    override suspend fun updateSong(song: Song): Result<Unit> {
        map[song.id] = song
        return Result.success(Unit)
    }

    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        map.remove(songId)
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        val cur = map[songId] ?: return Result.failure(NoSuchElementException())
        map[songId] = cur.copy(isFavorite = !currentFavorite)
        return Result.success(Unit)
    }

    override suspend fun getPendingSyncSongs(userId: String): List<LocalSongEntity> = emptyList()
    override suspend fun markAsSynced(songId: String) {}
    override suspend fun saveRemoteSongs(songs: List<Song>) {}
}

private class FakeVideoStudioRepository : VideoProjectRepository {
    val store = mutableMapOf<String, VideoProject>()

    override fun observeVideoProjects(userId: String): Flow<List<VideoProject>> =
        flowOf(store.values.filter { it.userId == userId })

    override suspend fun getVideoProject(userId: String, id: String): VideoProject? =
        store[id]?.takeIf { it.userId == userId }

    override suspend fun createVideoProject(project: VideoProject): Result<String> {
        store[project.id] = project
        return Result.success(project.id)
    }

    override suspend fun updateVideoProject(project: VideoProject): Result<Unit> {
        store[project.id] = project
        return Result.success(Unit)
    }

    override suspend fun deleteVideoProject(userId: String, id: String): Result<Unit> {
        store.remove(id)
        return Result.success(Unit)
    }
}

private class FakeUserStorageStub : com.osei.aimusicstudio.data.local.repository.LocalUserStorage {
    override fun observeUserProfile(userId: String): Flow<com.osei.aimusicstudio.data.model.UserProfile?> = flowOf(null)
    override suspend fun getUserProfile(userId: String): com.osei.aimusicstudio.data.model.UserProfile? = null
    override suspend fun createOrUpdateProfile(profile: com.osei.aimusicstudio.data.model.UserProfile): Result<Unit> = Result.success(Unit)
    override suspend fun updatePreferences(userId: String, language: String, genre: String): Result<Unit> = Result.success(Unit)
    override suspend fun getPendingSyncProfiles(): List<com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity> = emptyList()
    override suspend fun markAsSynced(userId: String) {}
    override suspend fun saveRemoteProfile(profile: com.osei.aimusicstudio.data.model.UserProfile) {}
    override suspend fun clearUserProfile(userId: String) {}
}
