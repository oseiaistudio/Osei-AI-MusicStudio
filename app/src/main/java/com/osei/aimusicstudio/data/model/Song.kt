package com.osei.aimusicstudio.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Song(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val genre: String = "Ghanaian Highlife",
    val mood: String = "Uplifting",
    val vocalPreference: String = "Male",
    val language: String = "English",
    val tempo: String = "Moderate (110 BPM)",
    val lyrics: String = "",
    val isInstrumental: Boolean = false,
    val status: String = "draft", // draft, queued, generating, completed, failed
    val audioUrl: String = "",
    val durationSeconds: Long = 0L,
    val isFavorite: Boolean = false,
    val phaseVersion: String = "phase_1_preview",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "genre" to genre,
            "mood" to mood,
            "vocalPreference" to vocalPreference,
            "language" to language,
            "tempo" to tempo,
            "lyrics" to lyrics,
            "isInstrumental" to isInstrumental,
            "status" to status,
            "audioUrl" to audioUrl,
            "durationSeconds" to durationSeconds,
            "isFavorite" to isFavorite,
            "phaseVersion" to phaseVersion
        )
        return map
    }
}
