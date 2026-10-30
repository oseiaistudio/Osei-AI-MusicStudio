package com.osei.aimusicstudio.data.remote.model

/**
 * Server-authoritative request payload for AI music generation.
 * Contains user-specified parameters without any client-side credentials or secrets.
 */
data class MusicGenerationRequest(
    val songId: String,
    val userId: String,
    val title: String,
    val description: String = "",
    val genre: String,
    val mood: String,
    val vocalPreference: String,
    val language: String,
    val lyrics: String = "",
    val isInstrumental: Boolean = false,
    val modelTarget: String = "google-lyria-3-pro" // Officially documented Google music generation model
) {
    fun isValid(): Boolean {
        return songId.isNotBlank() &&
                userId.isNotBlank() &&
                title.isNotBlank() &&
                genre.isNotBlank() &&
                mood.isNotBlank()
    }
}
