package com.osei.aimusicstudio.data.local.repository

import com.osei.aimusicstudio.data.local.dao.UserProfileDao
import com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity
import com.osei.aimusicstudio.data.local.mapper.toDomainModel
import com.osei.aimusicstudio.data.local.mapper.toLocalEntity
import com.osei.aimusicstudio.data.model.UserProfile
import com.osei.aimusicstudio.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LocalUserStorage : UserRepository {
    suspend fun getPendingSyncProfiles(): List<LocalUserProfileEntity>
    suspend fun markAsSynced(userId: String)
    suspend fun saveRemoteProfile(profile: UserProfile)
    suspend fun clearUserProfile(userId: String)
}

class RoomUserRepository(
    private val userProfileDao: UserProfileDao
) : LocalUserStorage {

    override fun observeUserProfile(userId: String): Flow<UserProfile?> {
        return userProfileDao.observeUserProfile(userId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return userProfileDao.getUserProfile(userId)?.toDomainModel()
    }

    override suspend fun createOrUpdateProfile(profile: UserProfile): Result<Unit> {
        val now = System.currentTimeMillis()
        val existing = userProfileDao.getUserProfile(profile.userId)
        val syncStatus = if (existing == null) {
            LocalUserProfileEntity.SYNC_STATUS_PENDING_UPDATE
        } else {
            LocalUserProfileEntity.SYNC_STATUS_PENDING_UPDATE
        }

        val entity = profile.toLocalEntity(
            syncStatus = syncStatus
        ).copy(updatedAtEpoch = now)

        return try {
            userProfileDao.insertOrUpdate(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreferences(userId: String, language: String, genre: String): Result<Unit> {
        val now = System.currentTimeMillis()
        return try {
            userProfileDao.updatePreferences(
                userId = userId,
                language = language,
                genre = genre,
                updatedAt = now,
                syncStatus = LocalUserProfileEntity.SYNC_STATUS_PENDING_UPDATE
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingSyncProfiles(): List<LocalUserProfileEntity> {
        return userProfileDao.getPendingSyncProfiles()
    }

    override suspend fun markAsSynced(userId: String) {
        userProfileDao.markAsSynced(userId)
    }

    override suspend fun saveRemoteProfile(profile: UserProfile) {
        val entity = profile.toLocalEntity(
            syncStatus = LocalUserProfileEntity.SYNC_STATUS_SYNCED,
            lastSyncedAt = System.currentTimeMillis()
        )
        userProfileDao.insertOrUpdate(entity)
    }

    override suspend fun clearUserProfile(userId: String) {
        userProfileDao.deleteUserProfile(userId)
    }
}
