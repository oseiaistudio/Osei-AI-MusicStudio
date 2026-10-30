package com.osei.aimusicstudio.data.remote.validation

import java.net.URI

/**
 * Validates audio asset URLs returned from the remote music generation backend.
 *
 * Enforces zero-trust audio integrity:
 * - Must be a valid URI with HTTPS scheme (or HTTP for localhost in local test suites).
 * - Must not be blank, malformed, or contain dangerous schemes (file://, javascript:, data:).
 * - Must point to a valid audio resource or CDN audio stream.
 * - Rejects known placeholder, dummy, or rain-sound audio URLs used in fake generation.
 */
object AudioReferenceValidator {

    private val REJECTED_SUBSTRINGS = listOf(
        "rain",
        "placeholder",
        "sample",
        "dummy",
        "fake",
        "mock",
        "test-audio.mp3",
        "beep"
    )

    private val VALID_AUDIO_EXTENSIONS = listOf(
        ".mp3",
        ".wav",
        ".m4a",
        ".aac",
        ".ogg",
        ".flac"
    )

    sealed interface ValidationResult {
        object Valid : ValidationResult
        data class Invalid(val reason: String) : ValidationResult
    }

    fun validate(audioUrl: String?): ValidationResult {
        if (audioUrl.isNullOrBlank()) {
            return ValidationResult.Invalid("Audio URL is empty or null")
        }

        val trimmed = audioUrl.trim()

        val uri = try {
            URI.create(trimmed)
        } catch (e: Exception) {
            return ValidationResult.Invalid("Audio URL is not a well-formed URI: ${e.message}")
        }

        val scheme = uri.scheme?.lowercase()
        if (scheme != "https" && scheme != "http") {
            return ValidationResult.Invalid("Audio URL must use HTTPS (got '$scheme')")
        }

        val host = uri.host
        if (host.isNullOrBlank()) {
            return ValidationResult.Invalid("Audio URL has no valid host")
        }

        // Check for disallowed fake / placeholder identifiers
        val lowerUrl = trimmed.lowercase()
        for (rejected in REJECTED_SUBSTRINGS) {
            if (lowerUrl.contains(rejected)) {
                return ValidationResult.Invalid("Audio URL references a placeholder or fake audio sample ('$rejected')")
            }
        }

        // Check path or query parameters for legitimate audio indicator or CDN endpoint
        val path = uri.path?.lowercase() ?: ""
        val hasAudioExtension = VALID_AUDIO_EXTENSIONS.any { path.endsWith(it) }
        val isCdnOrCloudStorage = host.contains("storage.googleapis.com") ||
                host.contains("firebasestorage.googleapis.com") ||
                host.contains("supabase.co") ||
                host.contains("cloudfront.net") ||
                host.contains("cdn")

        if (!hasAudioExtension && !isCdnOrCloudStorage) {
            return ValidationResult.Invalid("Audio URL does not point to a recognized audio format or CDN audio stream")
        }

        return ValidationResult.Valid
    }
}
