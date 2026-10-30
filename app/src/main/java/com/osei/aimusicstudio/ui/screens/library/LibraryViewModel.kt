package com.osei.aimusicstudio.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osei.aimusicstudio.core.UiState
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryFilterState(
    val selectedGenre: String = "All",
    val searchQuery: String = "",
    val favoritesOnly: Boolean = false
)

class LibraryViewModel(
    private val songRepository: SongRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _filterState = MutableStateFlow(LibraryFilterState())
    val filterState: StateFlow<LibraryFilterState> = _filterState.asStateFlow()

    private val rawSongsFlow = songRepository.observeSongs(currentUserId)
        .catch { emit(emptyList()) }

    val libraryUiState: StateFlow<UiState<List<Song>>> = combine(rawSongsFlow, _filterState) { songs, filter ->
        val filtered = songs.filter { song ->
            val matchesGenre = filter.selectedGenre == "All" || song.genre.equals(filter.selectedGenre, ignoreCase = true)
            val matchesSearch = filter.searchQuery.isBlank() ||
                song.title.contains(filter.searchQuery, ignoreCase = true) ||
                song.description.contains(filter.searchQuery, ignoreCase = true) ||
                song.lyrics.contains(filter.searchQuery, ignoreCase = true)
            val matchesFav = !filter.favoritesOnly || song.isFavorite

            matchesGenre && matchesSearch && matchesFav
        }

        if (filtered.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(filtered)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading
    )

    fun setGenreFilter(genre: String) {
        _filterState.update { it.copy(selectedGenre = genre) }
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavoritesOnly() {
        _filterState.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.toggleFavorite(currentUserId, song.id, song.isFavorite)
        }
    }

    fun deleteSong(songId: String) {
        viewModelScope.launch {
            songRepository.deleteSong(currentUserId, songId)
        }
    }
}
