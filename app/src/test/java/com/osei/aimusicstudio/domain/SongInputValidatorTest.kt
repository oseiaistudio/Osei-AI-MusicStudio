package com.osei.aimusicstudio.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SongInputValidatorTest {

    private val validator = SongInputValidator()

    @Test
    fun validateTitle_validTitle_returnsValid() {
        val result = validator.validateTitle("Highlife in Accra")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun validateTitle_emptyTitle_returnsInvalid() {
        val result = validator.validateTitle("   ")
        assertTrue(result is ValidationResult.Invalid)
        val error = (result as ValidationResult.Invalid).error
        assertTrue(error.contains("required", ignoreCase = true))
    }

    @Test
    fun validateTitle_shortTitle_returnsInvalid() {
        val result = validator.validateTitle("Hi")
        assertTrue(result is ValidationResult.Invalid)
        val error = (result as ValidationResult.Invalid).error
        assertTrue(error.contains("at least 3", ignoreCase = true))
    }

    @Test
    fun validateTitle_tooLongTitle_returnsInvalid() {
        val longTitle = "A".repeat(85)
        val result = validator.validateTitle(longTitle)
        assertTrue(result is ValidationResult.Invalid)
        val error = (result as ValidationResult.Invalid).error
        assertTrue(error.contains("cannot exceed", ignoreCase = true))
    }

    @Test
    fun validateDescription_validDescription_returnsValid() {
        val result = validator.validateDescription("A classic highlife rhythm featuring brass and rhythm guitars.")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun validateDescription_tooLong_returnsInvalid() {
        val longDesc = "A".repeat(505)
        val result = validator.validateDescription(longDesc)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateLyrics_validLyrics_returnsValid() {
        val result = validator.validateLyrics("[Verse 1]\nEarly morning sun rising over Kumasi")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun validateGenre_knownGenre_returnsValid() {
        assertTrue(validator.validateGenre("Ghanaian Highlife") is ValidationResult.Valid)
        assertTrue(validator.validateGenre("Afrobeats") is ValidationResult.Valid)
        assertTrue(validator.validateGenre("Hiplife") is ValidationResult.Valid)
    }

    @Test
    fun validateGenre_unknownGenre_returnsInvalid() {
        assertTrue(validator.validateGenre("Polka") is ValidationResult.Invalid)
    }

    @Test
    fun validateMood_knownMood_returnsValid() {
        assertTrue(validator.validateMood("Uplifting") is ValidationResult.Valid)
        assertTrue(validator.validateMood("Energetic") is ValidationResult.Valid)
    }

    @Test
    fun songInputFormState_validState_isValidReturnsTrue() {
        val form = SongInputFormState(
            title = "Golden Coast Highlife",
            genre = "Ghanaian Highlife",
            mood = "Uplifting"
        )
        assertTrue(form.isValid)
    }

    @Test
    fun songInputFormState_invalidTitle_isValidReturnsFalse() {
        val form = SongInputFormState(
            title = "",
            titleError = "Title is required"
        )
        assertFalse(form.isValid)
    }
}
