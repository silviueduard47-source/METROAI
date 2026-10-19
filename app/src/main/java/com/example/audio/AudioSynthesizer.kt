package com.example.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object AudioSynthesizer {

    /**
     * Creates a playable PCM 16-bit WAV file with a pleasant melodic phrase
     * for demo clips, allowing users to test playback immediately even before recording.
     */
    fun createDemoWavFile(context: Context, filename: String, durationSeconds: Int, baseFreq: Double = 440.0): File {
        val dir = File(context.cacheDir, "demo_audio").apply { mkdirs() }
        val file = File(dir, filename)
        if (file.exists() && file.length() > 44) {
            return file
        }

        val sampleRate = 44100
        val numSamples = sampleRate * durationSeconds
        val numBytes = numSamples * 2 // 16-bit mono = 2 bytes per sample

        val buffer = ByteBuffer.allocate(44 + numBytes).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + numBytes)
        buffer.put("WAVE".toByteArray())

        // fmt chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size (16 for PCM)
        buffer.putShort(1) // AudioFormat (1 = PCM)
        buffer.putShort(1) // NumChannels (1 = Mono)
        buffer.putInt(sampleRate) // SampleRate
        buffer.putInt(sampleRate * 2) // ByteRate
        buffer.putShort(2) // BlockAlign
        buffer.putShort(16) // BitsPerSample

        // data chunk
        buffer.put("data".toByteArray())
        buffer.putInt(numBytes)

        // Audio synthesis: gentle arpeggio with harmonics and envelope
        val notes = doubleArrayOf(baseFreq, baseFreq * 1.25, baseFreq * 1.5, baseFreq * 1.875, baseFreq * 2.0)
        val noteDuration = sampleRate / 2 // Change note every 0.5s

        for (i in 0 until numSamples) {
            val noteIndex = ((i / noteDuration) % notes.size).toInt()
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate

            // Fundamental + harmonic
            val wave = 0.7 * sin(2.0 * Math.PI * freq * t) + 0.3 * sin(4.0 * Math.PI * freq * t)

            // Attack & release envelope per note
            val notePos = (i % noteDuration).toDouble() / noteDuration
            val envelope = when {
                notePos < 0.1 -> notePos / 0.1
                notePos > 0.8 -> (1.0 - notePos) / 0.2
                else -> 1.0
            }

            val sample = (wave * envelope * 0.45 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            buffer.putShort(sample)
        }

        FileOutputStream(file).use { fos ->
            fos.write(buffer.array())
        }

        return file
    }
}
