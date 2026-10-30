package com.osei.aimusicstudio.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_songs",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["syncStatus"])
    ]
)
data class LocalSongEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val genre: String,
    val mood: String,
    val vocalPreference: String,
    val language: String,
    val tempo: String = "Moderate (110 BPM)",
    val lyrics: String,
    val isInstrumental: Boolean,
    val status: String,
    val audioUrl: String,
    val durationSeconds: Long,
    val isFavorite: Boolean,
    val phaseVersion: String,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    val syncStatus: String = SYNC_STATUS_SYNCED, // SYNCED, PENDING_INSERT, PENDING_UPDATE, PENDING_DELETE
    val lastSyncedAtEpoch: Long? = null
) {
    companion object {
        const val SYNC_STATUS_SYNCED = "SYNCED"
        const val SYNC_STATUS_PENDING_INSERT = "PENDING_INSERT"
        const val SYNC_STATUS_PENDING_UPDATE = "PENDING_UPDATE"
        const val SYNC_STATUS_PENDING_DELETE = "PENDING_DELETE"
    }
}
