package com.osei.aimusicstudio.ui.screens.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osei.aimusicstudio.core.UiState
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.model.StudioConstants
import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import com.osei.aimusicstudio.data.remote.service.DisabledMusicGenerationService
import com.osei.aimusicstudio.data.remote.service.MusicGenerationService
import com.osei.aimusicstudio.data.repository.SongRepository
import com.osei.aimusicstudio.domain.SongInputFormState
import com.osei.aimusicstudio.domain.SongInputValidator
import com.osei.aimusicstudio.domain.ValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenerationProgressState(
    val currentStep: Int = 0,
    val stepTitle: String = "",
    val stepDescription: String = "",
    val percent: Float = 0f,
    val isDone: Boolean = false,
    val generatedSong: Song? = null,
    val disclaimerMessage: String = "Draft saved locally — AI music generation is disabled.",
    val isCancelled: Boolean = false,
    val isLocalDraftOnly: Boolean = false
)

class SongGeneratorViewModel(
    private val songRepository: SongRepository,
    private val currentUserId: String,
    private val validator: SongInputValidator = SongInputValidator(),
    private val generationService: MusicGenerationService = DisabledMusicGenerationService()
) : ViewModel() {

    private val _formState = MutableStateFlow(SongInputFormState())
    val formState: StateFlow<SongInputFormState> = _formState.asStateFlow()

    private val _generationState = MutableStateFlow<UiState<Song>>(UiState.Empty)
    val generationState: StateFlow<UiState<Song>> = _generationState.asStateFlow()

    private val _progressState = MutableStateFlow(GenerationProgressState())
    val progressState: StateFlow<GenerationProgressState> = _progressState.asStateFlow()

    val localDrafts: StateFlow<List<Song>> = songRepository.observeSongs(currentUserId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var activeGenerationJob: Job? = null

    fun updateTitle(title: String) {
        val result = validator.validateTitle(title)
        val error = if (result is ValidationResult.Invalid) result.error else null
        _formState.update { it.copy(title = title, titleError = error) }
    }

    fun updateDescription(description: String) {
        val result = validator.validateDescription(description)
        val error = if (result is ValidationResult.Invalid) result.error else null
        _formState.update { it.copy(description = description, descriptionError = error) }
    }

    fun updateGenre(genre: String) {
        _formState.update { it.copy(genre = genre) }
    }

    fun updateMood(mood: String) {
        _formState.update { it.copy(mood = mood) }
    }

    fun updateTempo(tempo: String) {
        _formState.update { it.copy(tempo = tempo) }
    }

    fun updateVocalPreference(vocal: String) {
        _formState.update {
            it.copy(
                vocalPreference = vocal,
                isInstrumental = (vocal == "Instrumental Only")
            )
        }
    }

    fun updateLanguage(language: String) {
        _formState.update { it.copy(language = language) }
    }

    fun updateLyrics(lyrics: String) {
        val result = validator.validateLyrics(lyrics)
        val error = if (result is ValidationResult.Invalid) result.error else null
        _formState.update { it.copy(lyrics = lyrics, lyricsError = error) }
    }

    fun updateIsInstrumental(isInstrumental: Boolean) {
        _formState.update {
            it.copy(
                isInstrumental = isInstrumental,
                vocalPreference = if (isInstrumental) "Instrumental Only" else if (it.vocalPreference == "Instrumental Only") "Male" else it.vocalPreference
            )
        }
    }

    fun saveDraftLocally(onSaved: (Song) -> Unit = {}) {
        val current = _formState.value
        val titleVal = validator.validateTitle(current.title)
        if (titleVal is ValidationResult.Invalid) {
            _formState.update { it.copy(titleError = titleVal.error) }
            return
        }

        val descVal = validator.validateDescription(current.description)
        if (descVal is ValidationResult.Invalid) {
            _formState.update { it.copy(descriptionError = descVal.error) }
            return
        }

        val lyricsVal = validator.validateLyrics(current.lyrics)
        if (lyricsVal is ValidationResult.Invalid) {
            _formState.update { it.copy(lyricsError = lyricsVal.error) }
            return
        }

        viewModelScope.launch {
            _generationState.value = UiState.Loading
            val draft = Song(
                userId = currentUserId,
                title = current.title.trim(),
                description = current.description.trim(),
                genre = current.genre,
                mood = current.mood,
                tempo = current.tempo,
                vocalPreference = current.vocalPreference,
                language = current.language,
                lyrics = current.lyrics.trim(),
                isInstrumental = current.isInstrumental,
                status = "draft",
                audioUrl = "",
                durationSeconds = 0L,
                phaseVersion = "phase_4_foundation"
            )

            val createResult = songRepository.createSong(draft)
            if (createResult.isSuccess) {
                val savedId = createResult.getOrThrow()
                val persistedSong = draft.copy(id = savedId)
                _generationState.value = UiState.Success(persistedSong)
                onSaved(persistedSong)
            } else {
                _generationState.value = UiState.Error(
                    createResult.exceptionOrNull()?.localizedMessage ?: "Failed to save draft"
                )
            }
        }
    }

    fun deleteDraft(songId: String) {
        viewModelScope.launch {
            songRepository.deleteSong(currentUserId, songId)
        }
    }

    fun startGeneration(onNavigateToProgress: () -> Unit) {
        startGeneration(userToken = "", onNavigateToProgress = onNavigateToProgress)
    }

    fun startGeneration(
        userToken: String = "",
        onNavigateToProgress: () -> Unit
    ) {
        val current = _formState.value
        val titleVal = validator.validateTitle(current.title)
        if (titleVal is ValidationResult.Invalid) {
            _formState.update { it.copy(titleError = titleVal.error) }
            return
        }

        val descVal = validator.validateDescription(current.description)
        if (descVal is ValidationResult.Invalid) {
            _formState.update { it.copy(descriptionError = descVal.error) }
            return
        }

        val lyricsVal = validator.validateLyrics(current.lyrics)
        if (lyricsVal is ValidationResult.Invalid) {
            _formState.update { it.copy(lyricsError = lyricsVal.error) }
            return
        }

        onNavigateToProgress()

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            _generationState.value = UiState.Loading
            _progressState.value = GenerationProgressState(
                currentStep = 1,
                stepTitle = "Saving Song Draft Locally",
                stepDescription = "Validating Ghanaian & international rhythm scales and harmony tokens...",
                percent = 0.2f
            )

            // Save legitimate song draft record into Room (and optionally Firestore if sync enabled)
            val initialSong = Song(
                userId = currentUserId,
                title = current.title.trim(),
                description = current.description.trim(),
                genre = current.genre,
                mood = current.mood,
                tempo = current.tempo,
                vocalPreference = current.vocalPreference,
                language = current.language,
                lyrics = current.lyrics.trim(),
                isInstrumental = current.isInstrumental,
                status = "draft",
                audioUrl = "",
                durationSeconds = 0L,
                phaseVersion = "phase_4_foundation"
            )

            val createResult = songRepository.createSong(initialSong)
            if (createResult.isFailure) {
                _generationState.value = UiState.Error(
                    createResult.exceptionOrNull()?.localizedMessage ?: "Failed to save song draft"
                )
                return@launch
            }

            val savedSongId = createResult.getOrThrow()
            val queuedSong = initialSong.copy(id = savedSongId)

            if (!generationService.isConfigured || generationService.generationMode == "disabled") {
                _progressState.update {
                    it.copy(
                        currentStep = 1,
                        stepTitle = "Draft saved locally — AI music generation is disabled.",
                        stepDescription = "Song parameters, lyrics, and arrangement tokens are saved securely in your studio database. Remote synchronization and real AI music generation are disabled (Budget GH₵0).",
                        percent = 0.0f,
                        isDone = true,
                        isLocalDraftOnly = true,
                        generatedSong = queuedSong,
                        disclaimerMessage = "Draft saved locally — AI music generation is disabled. No external endpoints were contacted."
                    )
                }
                _generationState.value = UiState.Success(queuedSong)
            } else {
                val req = MusicGenerationRequest(
                    songId = savedSongId,
                    userId = currentUserId,
                    title = queuedSong.title,
                    description = queuedSong.description,
                    genre = queuedSong.genre,
                    mood = queuedSong.mood,
                    vocalPreference = queuedSong.vocalPreference,
                    language = queuedSong.language,
                    lyrics = queuedSong.lyrics,
                    isInstrumental = queuedSong.isInstrumental
                )

                val jobResult = generationService.submitGenerationJob(req, userToken)
                if (jobResult.isFailure) {
                    val err = jobResult.exceptionOrNull()?.localizedMessage ?: "Generation request failed on backend"
                    _generationState.value = UiState.Error(err)
                    _progressState.update {
                        it.copy(
                            currentStep = 1,
                            stepTitle = "Generation Failed",
                            stepDescription = err,
                            percent = 0.0f,
                            isDone = true,
                            generatedSong = queuedSong.copy(status = "failed")
                        )
                    }
                } else {
                    val jobResponse = jobResult.getOrThrow()
                    val updatedSong = queuedSong.copy(
                        status = jobResponse.status,
                        audioUrl = jobResponse.audioUrl.orEmpty(),
                        durationSeconds = jobResponse.durationSeconds
                    )
                    _progressState.update {
                        it.copy(
                            currentStep = if (jobResponse.isCompleted) 3 else 2,
                            stepTitle = if (jobResponse.isCompleted) "Generation Complete" else "Synthesizing Audio",
                            stepDescription = if (jobResponse.isCompleted) "Audio successfully synthesized and verified." else "Google Lyria model is processing audio tracks on Vertex AI...",
                            percent = if (jobResponse.isCompleted) 1.0f else (jobResponse.progressPercent / 100f).coerceAtLeast(0.3f),
                            isDone = jobResponse.isCompleted,
                            isLocalDraftOnly = false,
                            generatedSong = updatedSong
                        )
                    }
                    _generationState.value = UiState.Success(updatedSong)
                }
            }
        }
    }

    fun cancelGeneration() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _progressState.update {
            it.copy(
                stepTitle = "Generation Cancelled",
                stepDescription = "Song generation job was safely cancelled. No external charges were incurred.",
                percent = 0.0f,
                isDone = true,
                isCancelled = true
            )
        }
        _generationState.value = UiState.Empty
    }

    fun resetForm() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _formState.value = SongInputFormState()
        _generationState.value = UiState.Empty
        _progressState.value = GenerationProgressState()
    }
}
