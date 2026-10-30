package com.osei.aimusicstudio.remote

import com.osei.aimusicstudio.data.remote.validation.AudioReferenceValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioReferenceValidatorTest {

    @Test
    fun validate_nullOrBlank_returnsInvalid() {
        val resultNull = AudioReferenceValidator.validate(null)
        val resultBlank = AudioReferenceValidator.validate("   ")

        assertTrue(resultNull is AudioReferenceValidator.ValidationResult.Invalid)
        assertTrue(resultBlank is AudioReferenceValidator.ValidationResult.Invalid)
    }

    @Test
    fun validate_malformedUri_returnsInvalid() {
        val result = AudioReferenceValidator.validate("not a url at all :: //")
        assertTrue(result is AudioReferenceValidator.ValidationResult.Invalid)
    }

    @Test
    fun validate_insecureOrDisallowedScheme_returnsInvalid() {
        val fileScheme = AudioReferenceValidator.validate("file:///storage/emulated/0/song.mp3")
        val dataScheme = AudioReferenceValidator.validate("data:audio/mp3;base64,AAAA")
        val jsScheme = AudioReferenceValidator.validate("javascript:alert(1)")

        assertTrue(fileScheme is AudioReferenceValidator.ValidationResult.Invalid)
        assertTrue(dataScheme is AudioReferenceValidator.ValidationResult.Invalid)
        assertTrue(jsScheme is AudioReferenceValidator.ValidationResult.Invalid)
    }

    @Test
    fun validate_rejectedFakeKeywords_returnsInvalid() {
        val rainSound = AudioReferenceValidator.validate("https://storage.googleapis.com/assets/rain_sound_relaxing.mp3")
        val dummyAudio = AudioReferenceValidator.validate("https://storage.googleapis.com/assets/dummy_track.mp3")
        val placeholder = AudioReferenceValidator.validate("https://cdn.example.com/placeholder.wav")
        val sampleAudio = AudioReferenceValidator.validate("https://cdn.example.com/sample-audio.mp3")

        assertTrue(rainSound is AudioReferenceValidator.ValidationResult.Invalid)
        assertTrue(dummyAudio is AudioReferenceValidator.ValidationResult.Invalid)
        assertTrue(placeholder is AudioReferenceValidator.ValidationResult.Invalid)
        assertTrue(sampleAudio is AudioReferenceValidator.ValidationResult.Invalid)
    }

    @Test
    fun validate_validHttpsAudioUrl_returnsValid() {
        val gcsAudio = AudioReferenceValidator.validate("https://storage.googleapis.com/osei-music-vault/user_123/track_456.mp3")
        val firebaseAudio = AudioReferenceValidator.validate("https://firebasestorage.googleapis.com/v0/b/osei-studio.appspot.com/o/songs%2Ftrack_1.wav?alt=media")
        val cdnAudio = AudioReferenceValidator.validate("https://cdn.oseiaimusic.com/streams/rendered_track_789.flac")

        assertTrue(gcsAudio is AudioReferenceValidator.ValidationResult.Valid)
        assertTrue(firebaseAudio is AudioReferenceValidator.ValidationResult.Valid)
        assertTrue(cdnAudio is AudioReferenceValidator.ValidationResult.Valid)
    }
}
