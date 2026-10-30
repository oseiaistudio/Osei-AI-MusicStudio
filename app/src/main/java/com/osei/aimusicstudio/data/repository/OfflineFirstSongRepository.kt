package com.osei.aimusicstudio.data.repository

import com.osei.aimusicstudio.data.local.repository.LocalSongStorage
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.sync.StudioSyncCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Offline-first implementation of [SongRepository].
 *
 * Guarantees:
 * - Immediate local-first operations using Room [LocalSongStorage].
 * - When [isLocalOnly] is true (default, GH₵0 budget), zero network calls or Firestore operations are initiated.
 * - When [syncCoordinator] has cloud sync enabled, background synchronization is safely dispatched for non-draft items.
 * - Local drafts (status == "draft") are strictly excluded from cloud synchronization and remain on-device.
 * - Offline changes are retained in Room with pending sync status and are never overwritten or deleted on failure.
 */
class OfflineFirstSongRepository(
    private val localSongStorage: LocalSongStorage,
    private val syncCoordinator: StudioSyncCoordinator? = null,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SongRepository {

    val isLocalOnly: Boolean
        get() = syncCoordinator?.isSyncEnabled != true

    override fun observeSongs(userId: String): Flow<List<Song>> {
        // Trigger a background sync ONLY if cloud sync is explicitly enabled
        if (userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            externalScope.launch {
                try {
                    syncCoordinator.syncSongs(userId)
                } catch (_: Exception) {
                    // Silently fall back to cached local Room data
                }
            }
        }
        return localSongStorage.observeSongs(userId)
    }

    override suspend fun getSong(userId: String, songId: String): Song? {
        return localSongStorage.getSong(userId, songId)
    }

    override suspend fun createSong(song: Song): Result<String> {
        val localResult = localSongStorage.createSong(song)
        // Safeguard: Local drafts are strictly excluded from cloud upload
        if (localResult.isSuccess && song.userId.isNotBlank() && syncCoordinator != null && !isLocalOnly && song.status != "draft") {
            externalScope.launch {
                try {
                    syncCoordinator.syncSongs(song.userId)
                } catch (_: Exception) {
                    // Retained in Room with PENDING_INSERT
                }
            }
        }
        return localResult
    }

    override suspend fun updateSong(song: Song): Result<Unit> {
        val localResult = localSongStorage.updateSong(song)
        // Safeguard: Local drafts are strictly excluded from cloud upload
        if (localResult.isSuccess && song.userId.isNotBlank() && syncCoordinator != null && !isLocalOnly && song.status != "draft") {
            externalScope.launch {
                try {
                    syncCoordinator.syncSongs(song.userId)
                } catch (_: Exception) {
                    // Retained in Room with PENDING_UPDATE
                }
            }
        }
        return localResult
    }

    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        val localResult = localSongStorage.deleteSong(userId, songId)
        if (localResult.isSuccess && userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            externalScope.launch {
                try {
                    syncCoordinator.syncSongs(userId)
                } catch (_: Exception) {
                    // Retained in Room with PENDING_DELETE
                }
            }
        }
        return localResult
    }

    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        val localResult = localSongStorage.toggleFavorite(userId, songId, currentFavorite)
        if (localResult.isSuccess && userId.isNotBlank() && syncCoordinator != null && !isLocalOnly) {
            externalScope.launch {
                try {
                    syncCoordinator.syncSongs(userId)
                } catch (_: Exception) {
                    // Retained in Room with PENDING_UPDATE
                }
            }
        }
        return localResult
    }
}
