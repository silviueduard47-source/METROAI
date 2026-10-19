package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.data.AudioClip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    var currentClipId: String? = null
        private set

    var isPlaying: Boolean = false
        private set

    var isPaused: Boolean = false
        private set

    fun play(
        context: Context,
        clip: AudioClip,
        onProgress: (currentMs: Long, totalMs: Long) -> Unit,
        onComplete: () -> Unit
    ) {
        // If already playing this clip, toggle pause/resume
        if (currentClipId == clip.id && mediaPlayer != null) {
            if (isPlaying) {
                pause()
                return
            } else if (isPaused) {
                resume()
                return
            }
        }

        stop()

        try {
            val player = MediaPlayer()
            val targetFile = if (clip.filePath != null && File(clip.filePath).exists()) {
                File(clip.filePath)
            } else {
                // Synthesize demo wav for mock clips
                AudioSynthesizer.createDemoWavFile(
                    context = context,
                    filename = "demo_${clip.id.take(8)}.wav",
                    durationSeconds = (clip.durationMs / 1000).toInt().coerceIn(3, 15),
                    baseFreq = when (clip.id.hashCode() % 3) {
                        0 -> 440.0
                        1 -> 523.25
                        else -> 392.0
                    }
                )
            }

            player.setDataSource(targetFile.absolutePath)
            player.prepare()
            player.setOnCompletionListener {
                stop()
                onComplete()
            }

            player.start()
            mediaPlayer = player
            currentClipId = clip.id
            isPlaying = true
            isPaused = false

            val totalDuration = if (player.duration > 0) player.duration.toLong() else clip.durationMs

            progressJob = scope.launch {
                while (isActive && isPlaying) {
                    val current = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    onProgress(current, totalDuration)
                    delay(50)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error playing clip: ${clip.title}", e)
            stop()
            onComplete()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
                isPaused = true
                progressJob?.cancel()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error pausing player", e)
        }
    }

    fun resume() {
        try {
            if (isPaused && mediaPlayer != null) {
                mediaPlayer?.start()
                isPlaying = true
                isPaused = false
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error resuming player", e)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error seeking", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error stopping player", e)
        } finally {
            mediaPlayer = null
            currentClipId = null
            isPlaying = false
            isPaused = false
        }
    }
}
