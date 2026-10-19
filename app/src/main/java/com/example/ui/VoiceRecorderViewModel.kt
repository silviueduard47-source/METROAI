package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.audio.RecordingState
import com.example.data.AudioClip
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class PlaybackState(
    val clipId: String? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentMs: Long = 0L,
    val totalMs: Long = 0L
) {
    val progressFraction: Float
        get() = if (totalMs > 0) (currentMs.toFloat() / totalMs).coerceIn(0f, 1f) else 0f
}

data class RecorderUiState(
    val pivotIndex: Int = 0, // 0 = record, 1 = saved clips
    val recordingState: RecordingState = RecordingState.IDLE,
    val elapsedMs: Long = 0L,
    val liveAmplitude: Float = 0f,
    val amplitudeHistory: List<Float> = emptyList(),
    val isHighQuality: Boolean = true,
    val clips: List<AudioClip> = emptyList(),
    val playback: PlaybackState = PlaybackState(),
    val toastMessage: String? = null,
    val showAboutDialog: Boolean = false,
    val clipToDelete: AudioClip? = null
)

class VoiceRecorderViewModel(application: Application) : AndroidViewModel(application) {

    private val recorderManager = AudioRecorderManager()
    private val playerManager = AudioPlayerManager()

    private val _uiState = MutableStateFlow(RecorderUiState())
    val uiState: StateFlow<RecorderUiState> = _uiState.asStateFlow()

    private var recordTimerJob: Job? = null

    init {
        loadDefaultClips()
    }

    private fun loadDefaultClips() {
        val now = System.currentTimeMillis()
        val defaultClips = listOf(
            AudioClip(
                id = "mock-1",
                title = "Project Brainstorm",
                durationMs = 102000L, // 01:42
                filePath = null,
                createdAt = now - (2 * 3600 * 1000L), // 2 hrs ago
                fileSizeBytes = 1680000L,
                waveformData = generateSampleWaveform(36, seed = 1),
                isMock = true
            ),
            AudioClip(
                id = "mock-2",
                title = "Interview Notes - Metro UI",
                durationMs = 195000L, // 03:15
                filePath = null,
                createdAt = now - (24 * 3600 * 1000L), // Yesterday
                fileSizeBytes = 3250000L,
                waveformData = generateSampleWaveform(36, seed = 2),
                isMock = true
            ),
            AudioClip(
                id = "mock-3",
                title = "Guitar Riff Acoustic",
                durationMs = 48000L, // 00:48
                filePath = null,
                createdAt = now - (3 * 86400 * 1000L), // 3 days ago
                fileSizeBytes = 790000L,
                waveformData = generateSampleWaveform(36, seed = 3),
                isMock = true
            ),
            AudioClip(
                id = "mock-4",
                title = "Meeting Memo & Action Items",
                durationMs = 144000L, // 02:24
                filePath = null,
                createdAt = now - (5 * 86400 * 1000L),
                fileSizeBytes = 2380000L,
                waveformData = generateSampleWaveform(36, seed = 4),
                isMock = true
            ),
            AudioClip(
                id = "mock-5",
                title = "Quick Voice Memo",
                durationMs = 22000L, // 00:22
                filePath = null,
                createdAt = now - (7 * 86400 * 1000L),
                fileSizeBytes = 360000L,
                waveformData = generateSampleWaveform(36, seed = 5),
                isMock = true
            )
        )

        _uiState.value = _uiState.value.copy(clips = defaultClips)
    }

    private fun generateSampleWaveform(count: Int, seed: Int): List<Float> {
        val random = java.util.Random(seed.toLong())
        return List(count) {
            (0.15f + random.nextFloat() * 0.85f).coerceIn(0.1f, 1.0f)
        }
    }

    fun setPivot(index: Int) {
        _uiState.value = _uiState.value.copy(pivotIndex = index)
    }

    fun toggleHighQuality() {
        val nextQuality = !_uiState.value.isHighQuality
        _uiState.value = _uiState.value.copy(
            isHighQuality = nextQuality,
            toastMessage = if (nextQuality) "Quality set to High (192 kbps)" else "Quality set to Standard (96 kbps)"
        )
    }

    fun setShowAboutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAboutDialog = show)
    }

    fun requestDeleteClip(clip: AudioClip?) {
        _uiState.value = _uiState.value.copy(clipToDelete = clip)
    }

    fun confirmDeleteClip() {
        val target = _uiState.value.clipToDelete ?: return
        if (target.filePath != null) {
            try {
                File(target.filePath).delete()
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (_uiState.value.playback.clipId == target.id) {
            stopPlayback()
        }
        val updated = _uiState.value.clips.filter { it.id != target.id }
        _uiState.value = _uiState.value.copy(
            clips = updated,
            clipToDelete = null,
            toastMessage = "Clip '${target.title}' deleted"
        )
    }

    fun restoreDemoClips() {
        loadDefaultClips()
        _uiState.value = _uiState.value.copy(toastMessage = "Mock recordings restored")
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    // Recording Controls
    fun startRecording(context: Context) {
        // Stop any active playback first
        stopPlayback()

        val success = recorderManager.startRecording(context, _uiState.value.isHighQuality)
        if (success) {
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.RECORDING,
                elapsedMs = 0L,
                liveAmplitude = 0f,
                amplitudeHistory = emptyList()
            )

            startTimerAndAmplitudeTracker()
        } else {
            _uiState.value = _uiState.value.copy(
                toastMessage = "Unable to start recording. Check microphone permission."
            )
        }
    }

    fun pauseRecording() {
        if (recorderManager.pauseRecording()) {
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.PAUSED
            )
        }
    }

    fun resumeRecording() {
        if (recorderManager.resumeRecording()) {
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.RECORDING
            )
        }
    }

    fun stopRecording() {
        recordTimerJob?.cancel()
        recordTimerJob = null

        val newClip = recorderManager.stopRecording()
        if (newClip != null) {
            val updated = listOf(newClip) + _uiState.value.clips
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.IDLE,
                elapsedMs = 0L,
                liveAmplitude = 0f,
                amplitudeHistory = emptyList(),
                clips = updated,
                toastMessage = "Recording saved: ${newClip.formattedDuration}"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.IDLE,
                elapsedMs = 0L,
                liveAmplitude = 0f,
                amplitudeHistory = emptyList()
            )
        }
    }

    fun cancelRecording() {
        recordTimerJob?.cancel()
        recordTimerJob = null
        recorderManager.stopRecording(discard = true)
        _uiState.value = _uiState.value.copy(
            recordingState = RecordingState.IDLE,
            elapsedMs = 0L,
            liveAmplitude = 0f,
            amplitudeHistory = emptyList(),
            toastMessage = "Recording discarded"
        )
    }

    private fun startTimerAndAmplitudeTracker() {
        recordTimerJob?.cancel()
        recordTimerJob = viewModelScope.launch {
            val history = mutableListOf<Float>()
            while (isActive && recorderManager.state != RecordingState.IDLE) {
                val elapsed = recorderManager.getElapsedTimeMs()
                val amp = if (recorderManager.state == RecordingState.RECORDING) {
                    val rawAmp = recorderManager.getMaxAmplitude()
                    val normalized = (rawAmp.toFloat() / 32767f).coerceIn(0f, 1f)
                    history.add(normalized)
                    if (history.size > 50) history.removeAt(0)
                    normalized
                } else 0f

                _uiState.value = _uiState.value.copy(
                    elapsedMs = elapsed,
                    liveAmplitude = amp,
                    amplitudeHistory = history.toList()
                )

                delay(40) // 25 updates per second for ultra smooth timer and visualizer
            }
        }
    }

    // Playback Controls
    fun playClip(context: Context, clip: AudioClip) {
        val current = _uiState.value.playback
        if (current.clipId == clip.id) {
            if (current.isPlaying) {
                playerManager.pause()
                _uiState.value = _uiState.value.copy(
                    playback = current.copy(isPlaying = false, isPaused = true)
                )
                return
            } else if (current.isPaused) {
                playerManager.resume()
                _uiState.value = _uiState.value.copy(
                    playback = current.copy(isPlaying = true, isPaused = false)
                )
                return
            }
        }

        playerManager.play(
            context = context,
            clip = clip,
            onProgress = { currentMs, totalMs ->
                _uiState.value = _uiState.value.copy(
                    playback = PlaybackState(
                        clipId = clip.id,
                        isPlaying = true,
                        isPaused = false,
                        currentMs = currentMs,
                        totalMs = totalMs
                    )
                )
            },
            onComplete = {
                _uiState.value = _uiState.value.copy(
                    playback = PlaybackState(clipId = null, isPlaying = false, isPaused = false)
                )
            }
        )

        _uiState.value = _uiState.value.copy(
            playback = PlaybackState(
                clipId = clip.id,
                isPlaying = true,
                isPaused = false,
                currentMs = 0L,
                totalMs = clip.durationMs
            )
        )
    }

    fun seekPlayback(progress: Float) {
        val current = _uiState.value.playback
        if (current.totalMs > 0) {
            val targetMs = (current.totalMs * progress).toLong()
            playerManager.seekTo(targetMs)
            _uiState.value = _uiState.value.copy(
                playback = current.copy(currentMs = targetMs)
            )
        }
    }

    fun stopPlayback() {
        playerManager.stop()
        _uiState.value = _uiState.value.copy(
            playback = PlaybackState(clipId = null, isPlaying = false, isPaused = false)
        )
    }

    override fun onCleared() {
        super.onCleared()
        recordTimerJob?.cancel()
        recorderManager.stopRecording(discard = true)
        playerManager.stop()
    }
}
