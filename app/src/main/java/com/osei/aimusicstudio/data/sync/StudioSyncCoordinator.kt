package com.osei.aimusicstudio.data.sync

import android.util.Log
import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.local.mapper.toDomainModel
import com.osei.aimusicstudio.data.local.preferences.StudioPreferences
import com.osei.aimusicstudio.data.local.repository.LocalSongStorage
import com.osei.aimusicstudio.data.local.repository.LocalUserStorage
import com.osei.aimusicstudio.data.repository.FirestoreSongRepository
import com.osei.aimusicstudio.data.repository.FirestoreUserRepository
import com.osei.aimusicstudio.data.repository.SongRepository
import com.osei.aimusicstudio.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(
        val songsSynced: Int,
        val profileSynced: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncState()
    data class Error(
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncState()
}

class StudioSyncCoordinator(
    private val localSongStorage: LocalSongStorage,
    private val localUserStorage: LocalUserStorage,
    private val remoteSongRepository: SongRepository? = null,
    private val remoteUserRepository: UserRepository? = null,
    private val studioPreferences: StudioPreferences? = null,
    var isCloudSyncEnabled: Boolean = false
) {
    private val tag = "StudioSyncCoordinator"

    val isSyncEnabled: Boolean
        get() = studioPreferences?.isCloudSyncEnabled?.value ?: isCloudSyncEnabled

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun syncAll(userId: String): Result<Unit> {
        if (!isSyncEnabled) {
            _syncState.value = SyncState.Idle
            return Result.success(Unit) // Local-Only Mode: zero Firestore calls
        }
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank for sync"))
        }

        _syncState.value = SyncState.Syncing

        var totalSongsSynced = 0
        var profileSynced = false

        try {
            // 1. Sync User Profile
            val profileResult = syncUserProfile(userId)
            profileSynced = profileResult.isSuccess

            // 2. Sync Songs
            val songsResult = syncSongs(userId)
            if (songsResult.isSuccess) {
                totalSongsSynced = songsResult.getOrDefault(0)
            }

            _syncState.value = SyncState.Success(
                songsSynced = totalSongsSynced,
                profileSynced = profileSynced
            )
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Sync failed for user: $userId", e)
            _syncState.value = SyncState.Error(e.message ?: "Unknown sync error")
            return Result.failure(e)
        }
    }

    suspend fun syncSongs(userId: String): Result<Int> {
        if (!isSyncEnabled) {
            return Result.success(0) // Local-Only Mode: zero Firestore calls
        }
        val remoteRepo = remoteSongRepository
            ?: return Result.success(0) // Remote not configured yet, local operations stay intact

        var syncedCount = 0

        try {
            // Step 1: Push pending local changes to remote Firestore
            val pendingSongs = localSongStorage.getPendingSyncSongs(userId)
            for (pendingEntity in pendingSongs) {
                val song = pendingEntity.toDomainModel()
                // Safeguard: Local drafts are strictly local to Room and excluded from cloud upload
                if (song.status == "draft") {
                    continue
                }
                when (pendingEntity.syncStatus) {
                    LocalSongEntity.SYNC_STATUS_PENDING_INSERT -> {
                        val result = remoteRepo.createSong(song)
                        if (result.isSuccess) {
                            localSongStorage.markAsSynced(song.id)
                            syncedCount++
                        }
                    }
                    LocalSongEntity.SYNC_STATUS_PENDING_UPDATE -> {
                        val result = remoteRepo.updateSong(song)
                        if (result.isSuccess) {
                            localSongStorage.markAsSynced(song.id)
                            syncedCount++
                        }
                    }
                    LocalSongEntity.SYNC_STATUS_PENDING_DELETE -> {
                        val result = remoteRepo.deleteSong(userId, song.id)
                        if (result.isSuccess) {
                            // Now safe to purge permanently from Room local table
                            localSongStorage.deleteSong(userId, song.id)
                            syncedCount++
                        }
                    }
                }
            }

            // Step 2: Pull remote changes and update local cache
            val remoteSongs = remoteRepo.observeSongs(userId).firstOrNull() ?: emptyList()
            if (remoteSongs.isNotEmpty()) {
                // Do not clobber any local record that has un-synced edits
                val currentPendingIds = localSongStorage.getPendingSyncSongs(userId).map { it.id }.toSet()
                val songsToCache = remoteSongs.filterNot { it.id in currentPendingIds }
                if (songsToCache.isNotEmpty()) {
                    localSongStorage.saveRemoteSongs(songsToCache)
                }
            }

            return Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(tag, "Song sync error: ${e.message}", e)
            return Result.failure(e)
        }
    }

    suspend fun syncUserProfile(userId: String): Result<Unit> {
        if (!isSyncEnabled) {
            return Result.success(Unit) // Local-Only Mode: zero Firestore calls
        }
        val remoteRepo = remoteUserRepository
            ?: return Result.success(Unit)

        try {
            // Push pending profile
            val pendingProfiles = localUserStorage.getPendingSyncProfiles()
            val userPending = pendingProfiles.firstOrNull { it.userId == userId }
            if (userPending != null) {
                val domainProfile = localUserStorage.getUserProfile(userId)
                if (domainProfile != null) {
                    val result = remoteRepo.createOrUpdateProfile(domainProfile)
                    if (result.isSuccess) {
                        localUserStorage.markAsSynced(userId)
                    }
                }
            }

            // Pull remote profile
            val remoteProfile = remoteRepo.getUserProfile(userId)
            if (remoteProfile != null) {
                val hasPending = localUserStorage.getPendingSyncProfiles().any { it.userId == userId }
                if (!hasPending) {
                    localUserStorage.saveRemoteProfile(remoteProfile)
                }
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Profile sync error: ${e.message}", e)
            return Result.failure(e)
        }
    }
}
