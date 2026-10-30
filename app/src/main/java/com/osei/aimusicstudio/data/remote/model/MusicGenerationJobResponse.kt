package com.osei.aimusicstudio.data.remote.model

/**
 * Server-authoritative response representing generation job status.
 *
 * Status values:
 * - "queued": Job is accepted by server and waiting in processing queue.
 * - "generating": Model is actively synthesizing audio frames and lyrics.
 * - "completed": Audio has been produced, verified, and uploaded to storage.
 * - "failed": Model error, quota limit, policy block, or unverified output.
 */
data class MusicGenerationJobResponse(
    val jobId: String,
    val songId: String,
    val status: String,
    val progressPercent: Int = 0,
    val audioUrl: String? = null,
    val durationSeconds: Long = 0L,
    val errorMessage: String? = null,
    val providerModel: String? = null,
    val serverTimestamp: Long = System.currentTimeMillis()
) {
    val isCompleted: Boolean
        get() = status == "completed" && !audioUrl.isNullOrBlank()

    val isFailed: Boolean
        get() = status == "failed"

    val isPendingOrProcessing: Boolean
        get() = status == "queued" || status == "generating"
}
