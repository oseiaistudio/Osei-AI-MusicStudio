package com.osei.aimusicstudio.domain

import com.osei.aimusicstudio.data.model.StudioConstants

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val error: String) : ValidationResult
}

data class SongInputFormState(
    val title: String = "",
    val description: String = "",
    val genre: String = "Ghanaian Highlife",
    val mood: String = "Uplifting",
    val vocalPreference: String = "Male",
    val language: String = "English",
    val tempo: String = "Moderate (110 BPM)",
    val lyrics: String = "",
    val isInstrumental: Boolean = false,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val lyricsError: String? = null
) {
    val isValid: Boolean
        get() = titleError == null && descriptionError == null && lyricsError == null && title.trim().length >= StudioConstants.MIN_TITLE_LENGTH
}

class SongInputValidator {

    fun validateTitle(title: String): ValidationResult {
        val trimmed = title.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Song title is required")
            trimmed.length < StudioConstants.MIN_TITLE_LENGTH ->
                ValidationResult.Invalid("Title must be at least ${StudioConstants.MIN_TITLE_LENGTH} characters")
            trimmed.length > StudioConstants.MAX_TITLE_LENGTH ->
                ValidationResult.Invalid("Title cannot exceed ${StudioConstants.MAX_TITLE_LENGTH} characters")
            else -> ValidationResult.Valid
        }
    }

    fun validateDescription(description: String): ValidationResult {
        return if (description.length > StudioConstants.MAX_DESCRIPTION_LENGTH) {
            ValidationResult.Invalid("Description cannot exceed ${StudioConstants.MAX_DESCRIPTION_LENGTH} characters")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateLyrics(lyrics: String): ValidationResult {
        return if (lyrics.length > StudioConstants.MAX_LYRICS_LENGTH) {
            ValidationResult.Invalid("Custom lyrics cannot exceed ${StudioConstants.MAX_LYRICS_LENGTH} characters")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateGenre(genre: String): ValidationResult {
        return if (StudioConstants.GENRES.contains(genre)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Please select a recognized genre")
        }
    }

    fun validateMood(mood: String): ValidationResult {
        return if (StudioConstants.MOODS.contains(mood)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Please select a recognized mood")
        }
    }
}
