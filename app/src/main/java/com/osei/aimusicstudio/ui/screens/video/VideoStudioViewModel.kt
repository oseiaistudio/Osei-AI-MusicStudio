package com.osei.aimusicstudio.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.model.VideoProject
import com.osei.aimusicstudio.data.repository.SongRepository
import com.osei.aimusicstudio.data.repository.VideoProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class VideoStudioFormState(
    val title: String = "",
    val prompt: String = "",
    val visualStyle: String = "Afrofuturism 3D",
    val aspectRatio: String = "9:16",
    val linkedSongId: String? = null,
    val linkedSongTitle: String? = null,
    val localAudioUri: String? = null,
    val localVideoUri: String? = null,
    val titleError: String? = null,
    val promptError: String? = null,
    val isDraftSaved: Boolean = false,
    val savedDraftMessage: String? = null
)

class VideoStudioViewModel(
    private val videoProjectRepository: VideoProjectRepository,
    private val songRepository: SongRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _formState = MutableStateFlow(VideoStudioFormState())
    val formState: StateFlow<VideoStudioFormState> = _formState.asStateFlow()

    val videoProjects: StateFlow<List<VideoProject>> = videoProjectRepository.observeVideoProjects(currentUserId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableSongs: StateFlow<List<Song>> = songRepository.observeSongs(currentUserId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateTitle(title: String) {
        val error = if (title.trim().isEmpty()) "Project title is required" else null
        _formState.update { it.copy(title = title, titleError = error, savedDraftMessage = null) }
    }

    fun updatePrompt(prompt: String) {
        val error = if (prompt.trim().isEmpty()) "Visual prompt is required" else null
        _formState.update { it.copy(prompt = prompt, promptError = error, savedDraftMessage = null) }
    }

    fun updateVisualStyle(style: String) {
        _formState.update { it.copy(visualStyle = style, savedDraftMessage = null) }
    }

    fun updateAspectRatio(ratio: String) {
        _formState.update { it.copy(aspectRatio = ratio, savedDraftMessage = null) }
    }

    fun linkSong(song: Song) {
        _formState.update {
            it.copy(
                linkedSongId = song.id,
                linkedSongTitle = song.title,
                savedDraftMessage = null
            )
        }
    }

    fun unlinkSong() {
        _formState.update {
            it.copy(
                linkedSongId = null,
                linkedSongTitle = null,
                savedDraftMessage = null
            )
        }
    }

    fun setCustomAudioUri(uriString: String?) {
        _formState.update { it.copy(localAudioUri = uriString, savedDraftMessage = null) }
    }

    fun setCustomVideoUri(uriString: String?) {
        _formState.update { it.copy(localVideoUri = uriString, savedDraftMessage = null) }
    }

    fun saveVideoDraft(onSaved: (VideoProject) -> Unit = {}) {
        val current = _formState.value
        if (current.title.trim().isEmpty()) {
            _formState.update { it.copy(titleError = "Project title is required") }
            return
        }
        if (current.prompt.trim().isEmpty()) {
            _formState.update { it.copy(promptError = "Visual prompt is required") }
            return
        }

        viewModelScope.launch {
            val project = VideoProject(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                title = current.title.trim(),
                prompt = current.prompt.trim(),
                visualStyle = current.visualStyle,
                aspectRatio = current.aspectRatio,
                linkedSongId = current.linkedSongId,
                linkedSongTitle = current.linkedSongTitle,
                localAudioUri = current.localAudioUri,
                localVideoUri = current.localVideoUri, // Only non-null if real local file is picked
                status = "draft", // Strictly genuine draft: no fake generation
                durationSeconds = 0L,
                phaseVersion = "phase_4_foundation",
                createdAtEpoch = System.currentTimeMillis(),
                updatedAtEpoch = System.currentTimeMillis()
            )

            val result = videoProjectRepository.createVideoProject(project)
            if (result.isSuccess) {
                _formState.update {
                    it.copy(
                        isDraftSaved = true,
                        savedDraftMessage = "Video project draft \"${project.title}\" saved locally in Room."
                    )
                }
                onSaved(project)
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            videoProjectRepository.deleteVideoProject(currentUserId, id)
        }
    }

    fun resetForm() {
        _formState.value = VideoStudioFormState()
    }
}
