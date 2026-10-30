package com.osei.aimusicstudio.data.local.mapper

import com.osei.aimusicstudio.data.local.entity.LocalAudioProjectEntity
import com.osei.aimusicstudio.data.model.AudioProject

fun AudioProject.toLocalEntity(): LocalAudioProjectEntity {
    return LocalAudioProjectEntity(
        id = id,
        userId = userId,
        title = title,
        prompt = prompt,
        genre = genre,
        mood = mood,
        tempo = tempo,
        vocalPreference = vocalPreference,
        language = language,
        lyrics = lyrics,
        isInstrumental = isInstrumental,
        localAudioUri = localAudioUri,
        audioUrl = audioUrl,
        status = status,
        durationSeconds = durationSeconds,
        isFavorite = isFavorite,
        phaseVersion = phaseVersion,
        createdAtEpoch = createdAtEpoch,
        updatedAtEpoch = updatedAtEpoch,
        syncStatus = LocalAudioProjectEntity.SYNC_STATUS_LOCAL_ONLY
    )
}

fun LocalAudioProjectEntity.toDomainModel(): AudioProject {
    return AudioProject(
        id = id,
        userId = userId,
        title = title,
        prompt = prompt,
        genre = genre,
        mood = mood,
        tempo = tempo,
        vocalPreference = vocalPreference,
        language = language,
        lyrics = lyrics,
        isInstrumental = isInstrumental,
        localAudioUri = localAudioUri,
        audioUrl = audioUrl,
        status = status,
        durationSeconds = durationSeconds,
        isFavorite = isFavorite,
        phaseVersion = phaseVersion,
        createdAtEpoch = createdAtEpoch,
        updatedAtEpoch = updatedAtEpoch
    )
}

fun List<LocalAudioProjectEntity>.toDomainAudioModels(): List<AudioProject> = map { it.toDomainModel() }
