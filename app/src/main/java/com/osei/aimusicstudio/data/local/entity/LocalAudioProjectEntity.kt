package com.osei.aimusicstudio.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_audio_projects",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["syncStatus"])
    ]
)
data class LocalAudioProjectEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val prompt: String = "",
    val genre: String = "Ghanaian Highlife",
    val mood: String = "Uplifting",
    val tempo: String = "Moderate (110 BPM)",
    val vocalPreference: String = "Male",
    val language: String = "English",
    val lyrics: String = "",
    val isInstrumental: Boolean = false,
    val localAudioUri: String? = null,
    val audioUrl: String = "",
    val status: String = "draft",
    val durationSeconds: Long = 0L,
    val isFavorite: Boolean = false,
    val phaseVersion: String = "phase_4_foundation",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis(),
    val syncStatus: String = SYNC_STATUS_LOCAL_ONLY
) {
    companion object {
        const val SYNC_STATUS_LOCAL_ONLY = "LOCAL_ONLY"
        const val SYNC_STATUS_SYNCED = "SYNCED"
        const val SYNC_STATUS_PENDING_INSERT = "PENDING_INSERT"
        const val SYNC_STATUS_PENDING_UPDATE = "PENDING_UPDATE"
        const val SYNC_STATUS_PENDING_DELETE = "PENDING_DELETE"
    }
}

typealias AudioProjectEntity = LocalAudioProjectEntity
typealias AudioProject = LocalAudioProjectEntity
