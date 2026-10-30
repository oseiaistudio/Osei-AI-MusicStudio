package com.osei.aimusicstudio.data.local.mapper

import com.google.firebase.Timestamp
import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.model.Song
import java.util.Date

fun Song.toLocalEntity(
    syncStatus: String = LocalSongEntity.SYNC_STATUS_SYNCED,
    lastSyncedAt: Long? = null
): LocalSongEntity {
    val createdEpoch = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    val updatedEpoch = updatedAt?.toDate()?.time ?: createdEpoch

    return LocalSongEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        genre = genre,
        mood = mood,
        vocalPreference = vocalPreference,
        language = language,
        tempo = tempo,
        lyrics = lyrics,
        isInstrumental = isInstrumental,
        status = status,
        audioUrl = audioUrl,
        durationSeconds = durationSeconds,
        isFavorite = isFavorite,
        phaseVersion = phaseVersion,
        createdAtEpoch = createdEpoch,
        updatedAtEpoch = updatedEpoch,
        syncStatus = syncStatus,
        lastSyncedAtEpoch = lastSyncedAt
    )
}

fun LocalSongEntity.toDomainModel(): Song {
    return Song(
        id = id,
        userId = userId,
        title = title,
        description = description,
        genre = genre,
        mood = mood,
        vocalPreference = vocalPreference,
        language = language,
        tempo = tempo,
        lyrics = lyrics,
        isInstrumental = isInstrumental,
        status = status,
        audioUrl = audioUrl,
        durationSeconds = durationSeconds,
        isFavorite = isFavorite,
        phaseVersion = phaseVersion,
        createdAt = Timestamp(Date(createdAtEpoch)),
        updatedAt = Timestamp(Date(updatedAtEpoch))
    )
}

fun List<LocalSongEntity>.toDomainModels(): List<Song> = map { it.toDomainModel() }
