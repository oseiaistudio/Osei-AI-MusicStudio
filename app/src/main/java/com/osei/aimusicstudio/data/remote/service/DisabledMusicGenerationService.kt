package com.osei.aimusicstudio.data.remote.service

import com.osei.aimusicstudio.data.remote.model.MusicGenerationJobResponse
import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest

/**
 * Free development implementation of [MusicGenerationService] enforcing GENERATION_MODE=disabled.
 *
 * Guarantees:
 * - GH₵0 expenditure: zero network calls to external generative models or billable cloud services.
 * - Server status remains "NOT_CONFIGURED" / "QUEUED".
 * - Never returns fake audio or claims generation succeeded.
 * - Allows full UI testing, local Room storage, and Firestore parameter persistence.
 */
class DisabledMusicGenerationService(
    val reason: String = "Free Development Mode: Remote music generation is disabled (Budget GH₵0). No paid endpoints are contacted."
) : MusicGenerationService {

    override val isConfigured: Boolean = false
    override val generationMode: String = "disabled"

    override suspend fun submitGenerationJob(
        request: MusicGenerationRequest,
        userToken: String
    ): Result<MusicGenerationJobResponse> {
        return Result.failure(
            IllegalStateException(reason)
        )
    }

    override suspend fun pollJobStatus(
        jobId: String,
        userToken: String
    ): Result<MusicGenerationJobResponse> {
        return Result.failure(
            IllegalStateException(reason)
        )
    }

    override suspend fun cancelJob(
        jobId: String,
        userToken: String
    ): Result<Unit> {
        return Result.success(Unit)
    }
}
