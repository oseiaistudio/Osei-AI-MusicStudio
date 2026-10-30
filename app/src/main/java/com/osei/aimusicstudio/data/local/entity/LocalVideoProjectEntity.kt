package com.osei.aimusicstudio.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_video_projects",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"])
    ]
)
data class LocalVideoProjectEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val prompt: String,
    val visualStyle: String,
    val aspectRatio: String,
    val linkedSongId: String?,
    val linkedSongTitle: String?,
    val localAudioUri: String?,
    val localVideoUri: String?,
    val status: String,
    val durationSeconds: Long,
    val phaseVersion: String,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    val syncStatus: String = SYNC_STATUS_LOCAL_ONLY
) {
    companion object {
        const val SYNC_STATUS_LOCAL_ONLY = "LOCAL_ONLY"
    }
}

typealias VideoProjectEntity = LocalVideoProjectEntity
typealias VideoProject = LocalVideoProjectEntity
