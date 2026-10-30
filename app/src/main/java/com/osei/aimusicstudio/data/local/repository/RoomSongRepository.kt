package com.osei.aimusicstudio.data.local.repository

import com.osei.aimusicstudio.data.local.dao.SongDao
import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.local.mapper.toDomainModel
import com.osei.aimusicstudio.data.local.mapper.toDomainModels
import com.osei.aimusicstudio.data.local.mapper.toLocalEntity
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface LocalSongStorage : SongRepository {
    suspend fun getPendingSyncSongs(userId: String): List<LocalSongEntity>
    suspend fun markAsSynced(songId: String)
    suspend fun saveRemoteSongs(songs: List<Song>)
}

class RoomSongRepository(
    private val songDao: SongDao
) : LocalSongStorage {

    override fun observeSongs(userId: String): Flow<List<Song>> {
        return songDao.observeSongs(userId).map { entities ->
            entities.toDomainModels()
        }
    }

    override suspend fun getSong(userId: String, songId: String): Song? {
        val entity = songDao.getSongById(songId)
        return if (entity != null && entity.userId == userId && entity.syncStatus != LocalSongEntity.SYNC_STATUS_PENDING_DELETE) {
            entity.toDomainModel()
        } else {
            null
        }
    }

    override suspend fun createSong(song: Song): Result<String> {
        val songId = if (song.id.isNotBlank()) song.id else "song_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val entity = song.copy(id = songId).toLocalEntity(
            syncStatus = LocalSongEntity.SYNC_STATUS_PENDING_INSERT
        )
        return try {
            songDao.insertSong(entity)
            Result.success(songId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSong(song: Song): Result<Unit> {
        require(song.id.isNotBlank()) { "Song ID cannot be blank for update" }
        val now = System.currentTimeMillis()
        val entity = song.toLocalEntity(
            syncStatus = LocalSongEntity.SYNC_STATUS_PENDING_UPDATE
        ).copy(updatedAtEpoch = now)

        return try {
            songDao.updateSong(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        return try {
            val existing = songDao.getSongById(songId)
            if (existing != null) {
                if (existing.syncStatus == LocalSongEntity.SYNC_STATUS_PENDING_INSERT) {
                    // Not yet pushed to Firestore, safe to delete locally immediately
                    songDao.deleteSongById(songId)
                } else {
                    // Mark as pending delete for future Firestore synchronization
                    songDao.markSyncStatus(songId, LocalSongEntity.SYNC_STATUS_PENDING_DELETE, now)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        val now = System.currentTimeMillis()
        return try {
            songDao.toggleFavorite(
                songId = songId,
                isFavorite = !currentFavorite,
                updatedAt = now,
                syncStatus = LocalSongEntity.SYNC_STATUS_PENDING_UPDATE
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingSyncSongs(userId: String): List<LocalSongEntity> {
        return songDao.getPendingSyncSongs(userId)
    }

    override suspend fun markAsSynced(songId: String) {
        songDao.markAsSynced(songId)
    }

    override suspend fun saveRemoteSongs(songs: List<Song>) {
        val entities = songs.map { song ->
            song.toLocalEntity(
                syncStatus = LocalSongEntity.SYNC_STATUS_SYNCED,
                lastSyncedAt = System.currentTimeMillis()
            )
        }
        songDao.insertSongs(entities)
    }
}
