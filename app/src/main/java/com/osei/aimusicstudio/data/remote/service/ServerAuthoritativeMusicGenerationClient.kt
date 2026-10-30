package com.osei.aimusicstudio.data.remote.service

import com.osei.aimusicstudio.data.remote.model.MusicGenerationJobResponse
import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import com.osei.aimusicstudio.data.remote.validation.AudioReferenceValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Production implementation of [MusicGenerationService] communicating with an authorized
 * server-side Cloud Function or Cloud Run proxy.
 *
 * Security & Architecture rules:
 * - Direct provider API keys (Vertex AI / Google Cloud credentials) are NEVER stored in this client.
 * - Authenticates requests via HTTP Bearer header using the user's verified Firebase Auth ID Token.
 * - The server-side endpoint validates user identity, verifies credits, calls the Google Lyria API,
 *   uploads output audio to Google Cloud Storage / Firebase Storage, and updates the database.
 * - Every completed audio asset is inspected by [AudioReferenceValidator] to prevent fake or malformed audio.
 */
class ServerAuthoritativeMusicGenerationClient(
    private val serverBaseUrl: String? = null,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : MusicGenerationService {

    override val isConfigured: Boolean
        get() = !serverBaseUrl.isNullOrBlank()

    override val generationMode: String
        get() = if (isConfigured) "remote" else "disabled"

    override suspend fun submitGenerationJob(
        request: MusicGenerationRequest,
        userToken: String
    ): Result<MusicGenerationJobResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Music generation server endpoint is not configured. Server deployment and billing authorization required.")
            )
        }

        if (userToken.isBlank()) {
            return@withContext Result.failure(
                SecurityException("Authentication token is missing. User must be signed in with verified session.")
            )
        }

        if (!request.isValid()) {
            return@withContext Result.failure(
                IllegalArgumentException("Invalid generation request: required parameters (title, genre, mood) are missing.")
            )
        }

        try {
            val jsonBody = JSONObject().apply {
                put("songId", request.songId)
                put("userId", request.userId)
                put("title", request.title)
                put("description", request.description)
                put("genre", request.genre)
                put("mood", request.mood)
                put("vocalPreference", request.vocalPreference)
                put("language", request.language)
                put("lyrics", request.lyrics)
                put("isInstrumental", request.isInstrumental)
                put("modelTarget", request.modelTarget)
            }

            val httpRequest = Request.Builder()
                .url("$serverBaseUrl/submitJob")
                .header("Authorization", "Bearer $userToken")
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    parseServerError(response.code, responseBody)
                )
            }

            val jobResponse = parseJobResponse(responseBody)
            validateJobResponseAudio(jobResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pollJobStatus(
        jobId: String,
        userToken: String
    ): Result<MusicGenerationJobResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Music generation server endpoint is not configured.")
            )
        }

        if (userToken.isBlank()) {
            return@withContext Result.failure(
                SecurityException("Authentication token is missing.")
            )
        }

        try {
            val httpRequest = Request.Builder()
                .url("$serverBaseUrl/jobStatus?jobId=$jobId")
                .header("Authorization", "Bearer $userToken")
                .get()
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    parseServerError(response.code, responseBody)
                )
            }

            val jobResponse = parseJobResponse(responseBody)
            validateJobResponseAudio(jobResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelJob(
        jobId: String,
        userToken: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Music generation server endpoint is not configured.")
            )
        }

        try {
            val httpRequest = Request.Builder()
                .url("$serverBaseUrl/cancelJob?jobId=$jobId")
                .header("Authorization", "Bearer $userToken")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to cancel job: HTTP ${response.code}")
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseJobResponse(jsonString: String): MusicGenerationJobResponse {
        val json = JSONObject(jsonString)
        return MusicGenerationJobResponse(
            jobId = json.optString("jobId", ""),
            songId = json.optString("songId", ""),
            status = json.optString("status", "failed"),
            progressPercent = json.optInt("progressPercent", 0),
            audioUrl = json.optString("audioUrl").takeIf { it.isNotBlank() },
            durationSeconds = json.optLong("durationSeconds", 0L),
            errorMessage = json.optString("errorMessage").takeIf { it.isNotBlank() },
            providerModel = json.optString("providerModel").takeIf { it.isNotBlank() },
            serverTimestamp = json.optLong("serverTimestamp", System.currentTimeMillis())
        )
    }

    private fun validateJobResponseAudio(jobResponse: MusicGenerationJobResponse): Result<MusicGenerationJobResponse> {
        if (jobResponse.status == "completed") {
            val audioValidation = AudioReferenceValidator.validate(jobResponse.audioUrl)
            if (audioValidation is AudioReferenceValidator.ValidationResult.Invalid) {
                return Result.failure(
                    SecurityException("Server returned completed status but audio asset failed integrity check: ${audioValidation.reason}")
                )
            }
        }
        return Result.success(jobResponse)
    }

    private fun parseServerError(statusCode: Int, body: String): Exception {
        val message = try {
            JSONObject(body).optString("error", "HTTP $statusCode")
        } catch (_: Exception) {
            "HTTP $statusCode: $body"
        }

        return when (statusCode) {
            401 -> SecurityException("Unauthorized: Invalid or expired Firebase ID token. $message")
            403 -> SecurityException("Forbidden: User does not have generation permissions or insufficient credits. $message")
            429 -> Exception("Quota exceeded or rate limited: $message")
            else -> Exception("Generation backend error ($statusCode): $message")
        }
    }
}
