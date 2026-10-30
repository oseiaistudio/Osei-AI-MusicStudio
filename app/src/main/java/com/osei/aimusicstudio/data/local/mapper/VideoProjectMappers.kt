package com.osei.aimusicstudio.data.local.mapper

import com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity
import com.osei.aimusicstudio.data.model.VideoProject

fun VideoProject.toLocalEntity(): LocalVideoProjectEntity {
    return LocalVideoProjectEntity(
        id = id,
        userId = userId,
        title = title,
        prompt = prompt,
        visualStyle = visualStyle,
        aspectRatio = aspectRatio,
        linkedSongId = linkedSongId,
        linkedSongTitle = linkedSongTitle,
        localAudioUri = localAudioUri,
        localVideoUri = localVideoUri,
        status = status,
        durationSeconds = durationSeconds,
        phaseVersion = phaseVersion,
        createdAtEpoch = createdAtEpoch,
        updatedAtEpoch = updatedAtEpoch,
        syncStatus = LocalVideoProjectEntity.SYNC_STATUS_LOCAL_ONLY
    )
}

fun LocalVideoProjectEntity.toDomainModel(): VideoProject {
    return VideoProject(
        id = id,
        userId = userId,
        title = title,
        prompt = prompt,
        visualStyle = visualStyle,
        aspectRatio = aspectRatio,
        linkedSongId = linkedSongId,
        linkedSongTitle = linkedSongTitle,
        localAudioUri = localAudioUri,
        localVideoUri = localVideoUri,
        status = status,
        durationSeconds = durationSeconds,
        phaseVersion = phaseVersion,
        createdAtEpoch = createdAtEpoch,
        updatedAtEpoch = updatedAtEpoch
    )
}

fun List<LocalVideoProjectEntity>.toDomainVideoModels(): List<VideoProject> = map { it.toDomainModel() }
