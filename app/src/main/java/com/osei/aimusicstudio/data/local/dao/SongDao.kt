package com.osei.aimusicstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM local_songs WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY createdAtEpoch DESC")
    fun observeSongs(userId: String): Flow<List<LocalSongEntity>>

    @Query("SELECT * FROM local_songs WHERE id = :songId")
    suspend fun getSongById(songId: String): LocalSongEntity?

    @Query("SELECT * FROM local_songs WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingSyncSongs(userId: String): List<LocalSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: LocalSongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<LocalSongEntity>)

    @Update
    suspend fun updateSong(song: LocalSongEntity)

    @Query("DELETE FROM local_songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)

    @Query("UPDATE local_songs SET syncStatus = :syncStatus, updatedAtEpoch = :updatedAt WHERE id = :songId")
    suspend fun markSyncStatus(songId: String, syncStatus: String, updatedAt: Long)

    @Query("UPDATE local_songs SET syncStatus = :syncStatus, lastSyncedAtEpoch = :syncedAt WHERE id = :songId")
    suspend fun markAsSynced(songId: String, syncStatus: String = LocalSongEntity.SYNC_STATUS_SYNCED, syncedAt: Long = System.currentTimeMillis())

    @Query("UPDATE local_songs SET isFavorite = :isFavorite, updatedAtEpoch = :updatedAt, syncStatus = :syncStatus WHERE id = :songId")
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean, updatedAt: Long, syncStatus: String = LocalSongEntity.SYNC_STATUS_PENDING_UPDATE)

    @Query("DELETE FROM local_songs WHERE userId = :userId")
    suspend fun clearUserSongs(userId: String)
}
