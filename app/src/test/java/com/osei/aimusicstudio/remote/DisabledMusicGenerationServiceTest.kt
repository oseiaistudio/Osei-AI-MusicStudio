package com.osei.aimusicstudio.remote

import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import com.osei.aimusicstudio.data.remote.service.DisabledMusicGenerationService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisabledMusicGenerationServiceTest {

    private val service = DisabledMusicGenerationService()

    @Test
    fun serviceProperties_enforceDisabledMode() {
        assertFalse(service.isConfigured)
        assertEquals("disabled", service.generationMode)
    }

    @Test
    fun submitGenerationJob_failsWithoutNetworkCall() = runBlocking {
        val request = MusicGenerationRequest(
            songId = "song_123",
            userId = "user_456",
            title = "Golden Highlife",
            genre = "Ghanaian Highlife",
            mood = "Celebratory",
            vocalPreference = "Male",
            language = "Twi"
        )

        val result = service.submitGenerationJob(request, userToken = "test_token")
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalStateException)
        assertTrue(exception?.message?.contains("Budget GH₵0") == true)
        assertTrue(exception?.message?.contains("disabled") == true)
    }

    @Test
    fun pollJobStatus_failsWithoutNetworkCall() = runBlocking {
        val result = service.pollJobStatus("job_123", userToken = "test_token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun cancelJob_returnsSuccessLocally() = runBlocking {
        val result = service.cancelJob("job_123", userToken = "test_token")
        assertTrue(result.isSuccess)
    }
}
