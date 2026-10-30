package com.osei.aimusicstudio.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Domain model representing an AI Audio Project / Song draft.
 *
 * All audio projects operate in Local-Only Free Mode (Budget GH₵0).
 * Status defaults to "draft".
 */
@IgnoreExtraProperties
data class AudioProject(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
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
    val status: String = "draft", // draft, queued, generating, completed, failed
    val durationSeconds: Long = 0L,
    val isFavorite: Boolean = false,
    val phaseVersion: String = "phase_4_foundation",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
) {
    val isPlayableAudio: Boolean
        get() = !localAudioUri.isNullOrBlank() || audioUrl.isNotBlank()
}
