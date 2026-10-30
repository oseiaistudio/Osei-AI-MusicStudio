package com.osei.aimusicstudio.generator

import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.repository.SongRepository
import com.osei.aimusicstudio.ui.screens.generator.SongGeneratorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongGeneratorViewModelTest {

    private lateinit var mockRepository: FakeSongRepository
    private lateinit var viewModel: SongGeneratorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockRepository = FakeSongRepository()
        viewModel = SongGeneratorViewModel(
            songRepository = mockRepository,
            currentUserId = "test_user_123"
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateTitle_validTitle_clearsTitleError() {
        viewModel.updateTitle("Accra Beats")
        val state = viewModel.formState.value
        assertEquals("Accra Beats", state.title)
        assertNull(state.titleError)
    }

    @Test
    fun updateTitle_emptyTitle_setsTitleError() {
        viewModel.updateTitle("   ")
        val state = viewModel.formState.value
        assertNotNull(state.titleError)
    }

    @Test
    fun updateGenre_updatesState() {
        viewModel.updateGenre("Hiplife")
        assertEquals("Hiplife", viewModel.formState.value.genre)
    }

    @Test
    fun updateMood_updatesState() {
        viewModel.updateMood("Celebratory")
        assertEquals("Celebratory", viewModel.formState.value.mood)
    }

    @Test
    fun updateVocalPreference_instrumentalOnly_setsIsInstrumentalTrue() {
        viewModel.updateVocalPreference("Instrumental Only")
        val state = viewModel.formState.value
        assertEquals("Instrumental Only", state.vocalPreference)
        assertTrue(state.isInstrumental)
    }

    @Test
    fun updateIsInstrumental_toggled_syncsVocalPreference() {
        viewModel.updateIsInstrumental(true)
        assertTrue(viewModel.formState.value.isInstrumental)
        assertEquals("Instrumental Only", viewModel.formState.value.vocalPreference)

        viewModel.updateIsInstrumental(false)
        assertFalse(viewModel.formState.value.isInstrumental)
        assertEquals("Male", viewModel.formState.value.vocalPreference)
    }

    @Test
    fun resetForm_restoresDefaultState() {
        viewModel.updateTitle("Test Track")
        viewModel.updateGenre("Gospel")
        viewModel.resetForm()

        val state = viewModel.formState.value
        assertEquals("", state.title)
        assertEquals("Ghanaian Highlife", state.genre)
    }

    @Test
    fun startGeneration_savesQueuedSongWithoutFakeAudio() {
        var navigatedToProgress = false
        viewModel.updateTitle("Kofi Highlife Jam")
        viewModel.updateGenre("Ghanaian Highlife")
        viewModel.updateMood("Uplifting")
        viewModel.updateLyrics("Odo yewu, tie me nne")

        viewModel.startGeneration { navigatedToProgress = true }

        assertTrue(navigatedToProgress)
        val progress = viewModel.progressState.value
        assertTrue(progress.isDone)
        assertEquals("Draft saved locally — AI music generation is disabled.", progress.stepTitle)
        assertEquals(0.0f, progress.percent, 0.001f)
        assertTrue(progress.isLocalDraftOnly)

        val createdSong = mockRepository.songs.firstOrNull()
        assertNotNull(createdSong)
        assertEquals("draft", createdSong?.status)
        assertTrue(createdSong?.audioUrl.isNullOrEmpty())
        assertEquals(0L, createdSong?.durationSeconds)
        assertEquals("phase_4_foundation", createdSong?.phaseVersion)
    }

    @Test
    fun cancelGeneration_updatesProgressStateAndCancelsJob() {
        viewModel.updateTitle("Track to Cancel")
        viewModel.startGeneration { }

        viewModel.cancelGeneration()
        val progress = viewModel.progressState.value
        assertTrue(progress.isDone)
        assertTrue(progress.isCancelled)
        assertEquals("Generation Cancelled", progress.stepTitle)
    }
}

private class FakeSongRepository : SongRepository {
    val songs = mutableListOf<Song>()

    override fun observeSongs(userId: String): Flow<List<Song>> = flowOf(songs)
    override suspend fun getSong(userId: String, songId: String): Song? = songs.find { it.id == songId }
    override suspend fun createSong(song: Song): Result<String> {
        val id = "fake_song_${songs.size + 1}"
        songs.add(song.copy(id = id))
        return Result.success(id)
    }
    override suspend fun updateSong(song: Song): Result<Unit> {
        val index = songs.indexOfFirst { it.id == song.id }
        if (index >= 0) songs[index] = song
        return Result.success(Unit)
    }
    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        songs.removeAll { it.id == songId }
        return Result.success(Unit)
    }
    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        val index = songs.indexOfFirst { it.id == songId }
        if (index >= 0) songs[index] = songs[index].copy(isFavorite = !currentFavorite)
        return Result.success(Unit)
    }
}
