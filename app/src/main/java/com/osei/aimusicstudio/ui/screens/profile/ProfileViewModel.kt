package com.osei.aimusicstudio.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osei.aimusicstudio.core.UiState
import com.osei.aimusicstudio.data.model.UserProfile
import com.osei.aimusicstudio.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile>> = userRepository.observeUserProfile(currentUserId)
        .map { profile ->
            if (profile != null) UiState.Success(profile) else UiState.Empty
        }
        .catch { error ->
            emit(UiState.Error(error.localizedMessage ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    fun updatePreferences(language: String, genre: String) {
        viewModelScope.launch {
            userRepository.updatePreferences(currentUserId, language, genre)
        }
    }
}
