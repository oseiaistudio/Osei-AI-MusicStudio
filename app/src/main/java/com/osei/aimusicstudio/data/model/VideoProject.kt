package com.osei.aimusicstudio.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Domain model representing an AI Video Studio project.
 *
 * All video projects in Phase 4 operate in Local-Only Free Mode (Budget GH₵0).
 * Status defaults to "draft". No fabricated video URLs or simulated generation are permitted.
 */
@IgnoreExtraProperties
data class VideoProject(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val prompt: String = "",
    val visualStyle: String = "Afrofuturism 3D",
    val aspectRatio: String = "9:16", // 9:16 (TikTok/Reels/Shorts), 16:9 (Landscape), 1:1 (Square)
    val linkedSongId: String? = null,
    val linkedSongTitle: String? = null,
    val localAudioUri: String? = null,
    val localVideoUri: String? = null,
    val status: String = "draft", // draft, generating, completed, failed
    val durationSeconds: Long = 0L,
    val phaseVersion: String = "phase_4_foundation",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
) {
    val isPlayableVideo: Boolean
        get() = !localVideoUri.isNullOrBlank()

    companion object {
        val VISUAL_STYLES = listOf(
            "Afrofuturism 3D",
            "Highlife Vintage 1970s",
            "Accra Neon Cyberpunk",
            "Cinematic Film 4K",
            "Anime / Stylized Toon",
            "Abstract Soundwave Visualizer",
            "Golden Hour Documentary"
        )

        val ASPECT_RATIOS = listOf(
            "9:16" to "9:16 (TikTok / Reels / Shorts)",
            "16:9" to "16:9 (Landscape YouTube)",
            "1:1" to "1:1 (Square Instagram)"
        )
    }
}
