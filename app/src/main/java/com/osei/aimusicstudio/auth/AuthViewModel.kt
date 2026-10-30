package com.osei.aimusicstudio.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.osei.aimusicstudio.data.model.UserProfile
import com.osei.aimusicstudio.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAutoSigningIn: Boolean = true
)

class AuthViewModel(
    private val authManager: AuthManager = AuthManager(),
    private val userRepository: UserRepository? = null
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authManager.authStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authManager.currentUser
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun initAutoSignIn(context: Context) {
        val credentialManager = CredentialManager.create(context)
        authManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { user ->
                _uiState.update { it.copy(isAutoSigningIn = false, isLoading = false, errorMessage = null) }
                syncUserProfile(user)
            },
            onUnauthenticated = {
                _uiState.update { it.copy(isAutoSigningIn = false) }
            },
            scope = viewModelScope
        )
    }

    fun signInWithGoogle(context: Context) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credentialManager = CredentialManager.create(context)
        authManager.signInWithGoogle(
            context = context,
            credentialManager = credentialManager,
            scope = viewModelScope,
            onSuccess = { user ->
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                syncUserProfile(user)
            },
            onError = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            },
            onCancelled = {
                _uiState.update { it.copy(isLoading = false) }
            }
        )
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        val credentialManager = CredentialManager.create(context)
        authManager.signOut(context, credentialManager, viewModelScope) {
            _uiState.update { AuthUiState(isAutoSigningIn = false) }
            onComplete()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun syncUserProfile(user: FirebaseUser) {
        userRepository?.let { repo ->
            viewModelScope.launch {
                val existing = repo.getUserProfile(user.uid)
                if (existing == null) {
                    val profile = UserProfile(
                        userId = user.uid,
                        displayName = user.displayName ?: "Osei Creator",
                        email = user.email ?: "",
                        avatarUrl = user.photoUrl?.toString(),
                        credits = 50L,
                        subscriptionTier = "free"
                    )
                    repo.createOrUpdateProfile(profile)
                }
            }
        }
    }
}
