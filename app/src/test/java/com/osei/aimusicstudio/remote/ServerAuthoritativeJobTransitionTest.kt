package com.osei.aimusicstudio.remote

import com.osei.aimusicstudio.data.remote.model.MusicGenerationJobResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerAuthoritativeJobTransitionTest {

    @Test
    fun queuedJob_isPendingOrProcessing_notCompleted() {
        val job = MusicGenerationJobResponse(
            jobId = "job_1",
            songId = "song_1",
            status = "queued",
            progressPercent = 5,
            audioUrl = null
        )

        assertTrue(job.isPendingOrProcessing)
        assertFalse(job.isCompleted)
        assertFalse(job.isFailed)
    }

    @Test
    fun generatingJob_isPendingOrProcessing() {
        val job = MusicGenerationJobResponse(
            jobId = "job_2",
            songId = "song_2",
            status = "generating",
            progressPercent = 45,
            audioUrl = null
        )

        assertTrue(job.isPendingOrProcessing)
        assertFalse(job.isCompleted)
        assertFalse(job.isFailed)
    }

    @Test
    fun completedJob_withValidAudioUrl_isCompletedTrue() {
        val job = MusicGenerationJobResponse(
            jobId = "job_3",
            songId = "song_3",
            status = "completed",
            progressPercent = 100,
            audioUrl = "https://storage.googleapis.com/music/track_3.mp3",
            durationSeconds = 180L
        )

        assertTrue(job.isCompleted)
        assertFalse(job.isPendingOrProcessing)
        assertFalse(job.isFailed)
        assertEquals(180L, job.durationSeconds)
    }

    @Test
    fun completedJob_withEmptyAudioUrl_isCompletedFalse() {
        val job = MusicGenerationJobResponse(
            jobId = "job_4",
            songId = "song_4",
            status = "completed",
            progressPercent = 100,
            audioUrl = "", // Empty audio reference should never be treated as completed
            durationSeconds = 0L
        )

        assertFalse(job.isCompleted)
    }

    @Test
    fun failedJob_isFailedTrue_hasErrorMessage() {
        val job = MusicGenerationJobResponse(
            jobId = "job_5",
            songId = "song_5",
            status = "failed",
            progressPercent = 20,
            audioUrl = null,
            errorMessage = "Google Lyria model quota limit reached. Please retry in 60s."
        )

        assertTrue(job.isFailed)
        assertFalse(job.isCompleted)
        assertEquals("Google Lyria model quota limit reached. Please retry in 60s.", job.errorMessage)
    }
}
