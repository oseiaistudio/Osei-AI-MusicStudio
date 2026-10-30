package com.osei.aimusicstudio.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.remote.validation.AudioReferenceValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Media3AudioPlayer(context: Context) {

    private val appContext = context.applicationContext
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playerState.update { it.copy(isBuffering = true, playbackError = null) }
                }
                Player.STATE_READY -> {
                    val duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                    _playerState.update {
                        it.copy(
                            isBuffering = false,
                            durationMs = duration,
                            playbackError = null,
                            isCompleted = false
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    _playerState.update {
                        it.copy(
                            isPlaying = false,
                            isBuffering = false,
                            isCompleted = true,
                            currentPositionMs = it.durationMs
                        )
                    }
                    stopProgressTracking()
                }
                Player.STATE_IDLE -> {
                    _playerState.update { it.copy(isBuffering = false) }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressTracking()
            } else {
                stopProgressTracking()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Media3 ExoPlayer playback error: ${error.message}", error)
            _playerState.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    playbackError = "Playback error: ${error.localizedMessage ?: "Unable to stream audio"}"
                )
            }
            stopProgressTracking()
        }
    }

    private fun initPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(appContext).build().apply {
                addListener(playerListener)
            }
        }
    }

    fun playSong(song: Song, audioUrl: String) {
        if (song.status != "completed" || audioUrl.isBlank()) {
            _playerState.update {
                it.copy(
                    currentSong = song,
                    audioUrl = null,
                    playbackError = "Cannot play draft track: AI music generation is disabled. Only legitimately completed songs with verified audio can be played."
                )
            }
            return
        }

        val validation = AudioReferenceValidator.validate(audioUrl)
        if (validation is AudioReferenceValidator.ValidationResult.Invalid) {
            _playerState.update {
                it.copy(
                    currentSong = song,
                    audioUrl = null,
                    playbackError = "Audio verification failed: ${validation.reason}"
                )
            }
            return
        }

        initPlayer()
        val player = exoPlayer ?: return

        try {
            val mediaItem = MediaItem.fromUri(audioUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _playerState.update {
                it.copy(
                    currentSong = song,
                    audioUrl = audioUrl,
                    playbackError = null,
                    currentPositionMs = 0L,
                    isCompleted = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audio item", e)
            _playerState.update {
                it.copy(playbackError = "Failed to load audio: ${e.localizedMessage}")
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        if (_playerState.value.isCompleted) {
            seekTo(0L)
        }
        exoPlayer?.play()
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val safePos = positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(safePos)
        _playerState.update { it.copy(currentPositionMs = safePos) }
    }

    fun stop() {
        exoPlayer?.stop()
        stopProgressTracking()
        _playerState.update {
            it.copy(isPlaying = false, isBuffering = false, currentPositionMs = 0L)
        }
    }

    fun release() {
        stopProgressTracking()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive) {
                val currentPos = exoPlayer?.currentPosition ?: 0L
                val duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                _playerState.update {
                    it.copy(
                        currentPositionMs = currentPos,
                        durationMs = if (duration > 0) duration else it.durationMs
                    )
                }
                delay(250L)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    companion object {
        private const val TAG = "Media3AudioPlayer"
    }
}
