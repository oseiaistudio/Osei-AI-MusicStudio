package com.osei.aimusicstudio.remote

import com.osei.aimusicstudio.data.remote.model.MusicGenerationRequest
import com.osei.aimusicstudio.data.remote.service.ServerAuthoritativeMusicGenerationClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicGenerationAuthSecurityTest {

    @Test
    fun unconfiguredClient_isConfiguredFalse() {
        val client = ServerAuthoritativeMusicGenerationClient(serverBaseUrl = null)
        assertFalse(client.isConfigured)
    }

    @Test
    fun unconfiguredClient_submitJob_failsWithIllegalStateException() = runBlocking {
        val client = ServerAuthoritativeMusicGenerationClient(serverBaseUrl = "")
        val request = MusicGenerationRequest(
            songId = "s1",
            userId = "u1",
            title = "Track",
            genre = "Hiplife",
            mood = "Energetic",
            vocalPreference = "Male",
            language = "English"
        )

        val result = client.submitGenerationJob(request, userToken = "sample_token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertTrue(result.exceptionOrNull()?.message?.contains("not configured") == true)
    }

    @Test
    fun blankUserToken_submitJob_failsWithSecurityException() = runBlocking {
        val client = ServerAuthoritativeMusicGenerationClient(serverBaseUrl = "https://us-central1-demo.cloudfunctions.net/api")
        val request = MusicGenerationRequest(
            songId = "s2",
            userId = "u2",
            title = "Track",
            genre = "Hiplife",
            mood = "Energetic",
            vocalPreference = "Male",
            language = "English"
        )

        val result = client.submitGenerationJob(request, userToken = "   ")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Authentication token is missing") == true)
    }

    @Test
    fun blankUserToken_pollJobStatus_failsWithSecurityException() = runBlocking {
        val client = ServerAuthoritativeMusicGenerationClient(serverBaseUrl = "https://us-central1-demo.cloudfunctions.net/api")
        val result = client.pollJobStatus(jobId = "job_123", userToken = "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
