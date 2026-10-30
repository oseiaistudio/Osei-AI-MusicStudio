package com.osei.aimusicstudio.data.repository

/**
 * Architecture preparation for future Supabase backend integration (Phase 2).
 *
 * Per security guidelines:
 * - No service-role keys or backend secrets are embedded in the client.
 * - This interface establishes the contract for remote generation jobs,
 *   audio asset CDN storage, and webhook updates when configured.
 */
interface SupabaseMusicBackend {
    val isConfigured: Boolean

    suspend fun submitGenerationJob(
        songId: String,
        prompt: Map<String, Any>
    ): SupabaseBackendResult<String>

    suspend fun checkJobStatus(jobId: String): SupabaseBackendResult<SupabaseJobStatus>

    suspend fun getStreamUrl(audioPath: String): SupabaseBackendResult<String>
}

data class SupabaseJobStatus(
    val jobId: String,
    val status: String, // "pending", "processing", "completed", "failed"
    val progressPercent: Int,
    val audioUrl: String? = null,
    val errorMessage: String? = null
)

sealed interface SupabaseBackendResult<out T> {
    data class Success<T>(val data: T) : SupabaseBackendResult<T>
    data class NotConfigured(val message: String) : SupabaseBackendResult<Nothing>
    data class Failure(val error: String) : SupabaseBackendResult<Nothing>
}

class SupabaseMusicBackendPlaceholder : SupabaseMusicBackend {
    override val isConfigured: Boolean = false

    override suspend fun submitGenerationJob(
        songId: String,
        prompt: Map<String, Any>
    ): SupabaseBackendResult<String> {
        return SupabaseBackendResult.NotConfigured(
            "Supabase backend is prepared for Phase 2. Configure project URL and client key in Phase 2."
        )
    }

    override suspend fun checkJobStatus(jobId: String): SupabaseBackendResult<SupabaseJobStatus> {
        return SupabaseBackendResult.NotConfigured(
            "Supabase backend is prepared for Phase 2. Status polling will connect in Phase 2."
        )
    }

    override suspend fun getStreamUrl(audioPath: String): SupabaseBackendResult<String> {
        return SupabaseBackendResult.NotConfigured(
            "Audio CDN URL resolution will connect once Supabase Storage bucket is active."
        )
    }
}
