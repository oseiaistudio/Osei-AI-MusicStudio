package com.osei.aimusicstudio.data.repository

import com.osei.aimusicstudio.data.local.repository.LocalUserStorage
import com.osei.aimusicstudio.data.model.UserProfile
import com.osei.aimusicstudio.data.sync.StudioSyncCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OfflineFirstUserRepository(
    private val localUserStorage: LocalUserStorage,
    private val syncCoordinator: StudioSyncCoordinator? = null,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : UserRepository {

    val isLocalOnly: Boolean
        get() = syncCoordinator?.isSyncEnabled != true

    override fun observeUserProfile(userId: String): Flow<UserProfile?> {
        if (userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            externalScope.launch {
                try {
                    syncCoordinator.syncUserProfile(userId)
                } catch (_: Exception) {
                    // Silently fall back to cached local profile
                }
            }
        }
        return localUserStorage.observeUserProfile(userId)
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        val cached = localUserStorage.getUserProfile(userId)
        if (cached == null && userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            try {
                syncCoordinator.syncUserProfile(userId)
            } catch (_: Exception) {
                // Return cached
            }
            return localUserStorage.getUserProfile(userId)
        }
        return cached
    }

    override suspend fun createOrUpdateProfile(profile: UserProfile): Result<Unit> {
        val localResult = localUserStorage.createOrUpdateProfile(profile)
        if (localResult.isSuccess && profile.userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            externalScope.launch {
                try {
                    syncCoordinator.syncUserProfile(profile.userId)
                } catch (_: Exception) {
                    // Stored locally in Room with pending sync status
                }
            }
        }
        return localResult
    }

    override suspend fun updatePreferences(userId: String, language: String, genre: String): Result<Unit> {
        val localResult = localUserStorage.updatePreferences(userId, language, genre)
        if (localResult.isSuccess && userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            externalScope.launch {
                try {
                    syncCoordinator.syncUserProfile(userId)
                } catch (_: Exception) {
                    // Stored locally in Room with pending sync status
                }
            }
        }
        return localResult
    }
}
