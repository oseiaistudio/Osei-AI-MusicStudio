package com.osei.aimusicstudio.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_user_profiles",
    indices = [
        Index(value = ["syncStatus"])
    ]
)
data class LocalUserProfileEntity(
    @PrimaryKey
    val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val credits: Long,
    val subscriptionTier: String,
    val preferredLanguage: String,
    val preferredGenre: String,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    val syncStatus: String = SYNC_STATUS_SYNCED,
    val lastSyncedAtEpoch: Long? = null
) {
    companion object {
        const val SYNC_STATUS_SYNCED = "SYNCED"
        const val SYNC_STATUS_PENDING_INSERT = "PENDING_INSERT"
        const val SYNC_STATUS_PENDING_UPDATE = "PENDING_UPDATE"
    }
}
