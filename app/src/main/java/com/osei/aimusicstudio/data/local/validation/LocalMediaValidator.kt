package com.osei.aimusicstudio.data.local.validation

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object LocalMediaValidator {

    private const val TAG = "LocalMediaValidator"

    sealed interface MediaValidationResult {
        object Valid : MediaValidationResult
        data class Invalid(val reason: String) : MediaValidationResult
    }

    fun persistUriPermissionIfSupported(contentResolver: ContentResolver, uri: Uri): Boolean {
        return try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            true
        } catch (e: SecurityException) {
            Log.d(TAG, "Persistable URI permission not supported: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed taking persistable permission: ${e.message}")
            false
        }
    }

    fun validateVideoUri(context: Context, uriString: String?): MediaValidationResult {
        if (uriString.isNullOrBlank()) {
            return MediaValidationResult.Invalid("Video URI is empty or null")
        }

        val uri = try {
            Uri.parse(uriString.trim())
        } catch (e: Exception) {
            return MediaValidationResult.Invalid("Malformed video URI: ${e.message}")
        }

        return try {
            val type = context.contentResolver.getType(uri)
            if (type == null || !type.startsWith("video")) {
                return MediaValidationResult.Invalid("Selected file is not a video format (MIME: '$type')")
            }

            val stream = context.contentResolver.openInputStream(uri)
                ?: return MediaValidationResult.Invalid("Unable to open input stream for video")
            stream.close()

            MediaValidationResult.Valid
        } catch (e: SecurityException) {
            MediaValidationResult.Invalid("Access permission revoked or expired: ${e.message}")
        } catch (e: java.io.FileNotFoundException) {
            MediaValidationResult.Invalid("Video file not found or was deleted: ${e.message}")
        } catch (e: Exception) {
            MediaValidationResult.Invalid("Local video file is inaccessible: ${e.message}")
        }
    }

    fun validateAudioUri(context: Context, uriString: String?): MediaValidationResult {
        if (uriString.isNullOrBlank()) {
            return MediaValidationResult.Invalid("Audio URI is empty or null")
        }

        val uri = try {
            Uri.parse(uriString.trim())
        } catch (e: Exception) {
            return MediaValidationResult.Invalid("Malformed audio URI: ${e.message}")
        }

        return try {
            val type = context.contentResolver.getType(uri)
            if (type == null || !type.startsWith("audio")) {
                return MediaValidationResult.Invalid("Selected file is not an audio format (MIME: '$type')")
            }

            val stream = context.contentResolver.openInputStream(uri)
                ?: return MediaValidationResult.Invalid("Unable to open input stream for audio")
            stream.close()

            MediaValidationResult.Valid
        } catch (e: SecurityException) {
            MediaValidationResult.Invalid("Access permission revoked or expired: ${e.message}")
        } catch (e: java.io.FileNotFoundException) {
            MediaValidationResult.Invalid("Audio file not found or was deleted: ${e.message}")
        } catch (e: Exception) {
            MediaValidationResult.Invalid("Local audio file is inaccessible: ${e.message}")
        }
    }
}
