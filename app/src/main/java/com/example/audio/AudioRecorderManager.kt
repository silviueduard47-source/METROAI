package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.data.AudioClip
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

class AudioRecorderManager {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime: Long = 0L
    private var accumulatedTime: Long = 0L
    private var pauseStartTime: Long = 0L

    var state: RecordingState = RecordingState.IDLE
        private set

    private val recordedAmplitudes = mutableListOf<Float>()

    fun startRecording(context: Context, highQuality: Boolean = true): Boolean {
        try {
            stopRecording(discard = true)

            val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            currentOutputFile = File(recordingsDir, "REC_$timeStamp.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                if (highQuality) {
                    setAudioEncodingBitRate(192000)
                    setAudioSamplingRate(48000)
                } else {
                    setAudioEncodingBitRate(96000)
                    setAudioSamplingRate(44100)
                }
                setOutputFile(currentOutputFile?.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordingStartTime = System.currentTimeMillis()
            accumulatedTime = 0L
            pauseStartTime = 0L
            state = RecordingState.RECORDING
            recordedAmplitudes.clear()
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error starting recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            state = RecordingState.IDLE
            return false
        }
    }

    fun pauseRecording(): Boolean {
        if (state != RecordingState.RECORDING) return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                accumulatedTime += System.currentTimeMillis() - recordingStartTime
                pauseStartTime = System.currentTimeMillis()
                state = RecordingState.PAUSED
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error pausing recording", e)
            false
        }
    }

    fun resumeRecording(): Boolean {
        if (state != RecordingState.PAUSED) return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                recordingStartTime = System.currentTimeMillis()
                state = RecordingState.RECORDING
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error resuming recording", e)
            false
        }
    }

    fun getElapsedTimeMs(): Long {
        return when (state) {
            RecordingState.IDLE -> 0L
            RecordingState.RECORDING -> accumulatedTime + (System.currentTimeMillis() - recordingStartTime)
            RecordingState.PAUSED -> accumulatedTime
        }
    }

    fun getMaxAmplitude(): Int {
        if (state != RecordingState.RECORDING) return 0
        return try {
            val amp = mediaRecorder?.maxAmplitude ?: 0
            val normalized = (amp.toFloat() / 32767f).coerceIn(0f, 1f)
            recordedAmplitudes.add(normalized)
            amp
        } catch (e: Exception) {
            0
        }
    }

    fun stopRecording(discard: Boolean = false): AudioClip? {
        if (state == RecordingState.IDLE) return null

        val finalDuration = getElapsedTimeMs()
        var clip: AudioClip? = null

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.w("AudioRecorderManager", "Stop called in invalid state", e)
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error releasing media recorder", e)
        } finally {
            mediaRecorder = null
            state = RecordingState.IDLE
        }

        val file = currentOutputFile
        if (discard || file == null || !file.exists() || finalDuration < 500) {
            file?.delete()
            currentOutputFile = null
            return null
        }

        // Subsample recorded amplitudes for waveform visualization (e.g. 40 points)
        val waveformPoints = if (recordedAmplitudes.isNotEmpty()) {
            val count = 36
            val step = (recordedAmplitudes.size.toFloat() / count).coerceAtLeast(1f)
            (0 until count).map { i ->
                val idx = (i * step).toInt().coerceAtMost(recordedAmplitudes.lastIndex)
                recordedAmplitudes[idx].coerceIn(0.1f, 1.0f)
            }
        } else {
            List(36) { 0.2f }
        }

        val titleFormat = SimpleDateFormat("h:mm a - MMM d", Locale.US).format(Date())
        clip = AudioClip(
            id = UUID.randomUUID().toString(),
            title = "Recording $titleFormat",
            durationMs = finalDuration,
            filePath = file.absolutePath,
            createdAt = System.currentTimeMillis(),
            fileSizeBytes = file.length(),
            waveformData = waveformPoints,
            isMock = false
        )

        currentOutputFile = null
        return clip
    }
}
