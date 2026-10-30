package com.osei.aimusicstudio.data.local.mapper

import com.google.firebase.Timestamp
import com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity
import com.osei.aimusicstudio.data.model.UserProfile
import java.util.Date

fun UserProfile.toLocalEntity(
    syncStatus: String = LocalUserProfileEntity.SYNC_STATUS_SYNCED,
    lastSyncedAt: Long? = null
): LocalUserProfileEntity {
    val createdEpoch = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    val updatedEpoch = updatedAt?.toDate()?.time ?: createdEpoch

    return LocalUserProfileEntity(
        userId = userId,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        credits = credits,
        subscriptionTier = subscriptionTier,
        preferredLanguage = preferredLanguage,
        preferredGenre = preferredGenre,
        createdAtEpoch = createdEpoch,
        updatedAtEpoch = updatedEpoch,
        syncStatus = syncStatus,
        lastSyncedAtEpoch = lastSyncedAt
    )
}

fun LocalUserProfileEntity.toDomainModel(): UserProfile {
    return UserProfile(
        id = userId,
        userId = userId,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        credits = credits,
        subscriptionTier = subscriptionTier,
        preferredLanguage = preferredLanguage,
        preferredGenre = preferredGenre,
        createdAt = Timestamp(Date(createdAtEpoch)),
        updatedAt = Timestamp(Date(updatedAtEpoch))
    )
}
