package com.osei.aimusicstudio.data.remote.service

import com.osei.aimusicstudio.data.remote.model.MusicGenerationJobResponse
import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest

/**
 * Service contract for server-authoritative AI music generation.
 *
 * Requirements:
 * 1. Never embed provider API keys, Google Cloud service account keys, or secrets in Android client code.
 * 2. Authenticate requests using Firebase Auth ID token (`userToken`).
 * 3. Support asynchronous job submission and polling for real generative models.
 * 4. Transparently report if the backend service is unconfigured, avoiding any simulated success.
 */
interface MusicGenerationService {

    /**
     * True if a valid remote generation server endpoint is configured.
     * When false, the client gracefully informs the user that server deployment is required.
     */
    val isConfigured: Boolean

    /**
     * Generation mode: "disabled" for Free Development Mode, "remote" when connected to backend.
     */
    val generationMode: String

    /**
     * Submits a music generation request to the server-side endpoint.
     *
     * @param request Parameters for the song.
     * @param userToken Fresh Firebase Auth ID token identifying the user.
     * @return Result containing the initial job status (queued or generating).
     */
    suspend fun submitGenerationJob(
        request: MusicGenerationRequest,
        userToken: String
    ): Result<MusicGenerationJobResponse>

    /**
     * Queries the current status of an in-progress generation job.
     *
     * @param jobId The job identifier returned by [submitGenerationJob].
     * @param userToken Fresh Firebase Auth ID token.
     * @return Result containing current job status and audio reference if completed.
     */
    suspend fun pollJobStatus(
        jobId: String,
        userToken: String
    ): Result<MusicGenerationJobResponse>

    /**
     * Cancels an in-progress generation job on the server.
     */
    suspend fun cancelJob(
        jobId: String,
        userToken: String
    ): Result<Unit>
}
