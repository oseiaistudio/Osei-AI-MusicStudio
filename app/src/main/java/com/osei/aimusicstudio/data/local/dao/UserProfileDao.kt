package com.osei.aimusicstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM local_user_profiles WHERE userId = :userId LIMIT 1")
    fun observeUserProfile(userId: String): Flow<LocalUserProfileEntity?>

    @Query("SELECT * FROM local_user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): LocalUserProfileEntity?

    @Query("SELECT * FROM local_user_profiles WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncProfiles(): List<LocalUserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: LocalUserProfileEntity)

    @Update
    suspend fun updateProfile(profile: LocalUserProfileEntity)

    @Query("UPDATE local_user_profiles SET preferredLanguage = :language, preferredGenre = :genre, updatedAtEpoch = :updatedAt, syncStatus = :syncStatus WHERE userId = :userId")
    suspend fun updatePreferences(
        userId: String,
        language: String,
        genre: String,
        updatedAt: Long,
        syncStatus: String = LocalUserProfileEntity.SYNC_STATUS_PENDING_UPDATE
    )

    @Query("UPDATE local_user_profiles SET syncStatus = :syncStatus, lastSyncedAtEpoch = :syncedAt WHERE userId = :userId")
    suspend fun markAsSynced(
        userId: String,
        syncStatus: String = LocalUserProfileEntity.SYNC_STATUS_SYNCED,
        syncedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM local_user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)
}
