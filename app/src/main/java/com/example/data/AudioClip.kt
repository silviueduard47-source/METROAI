package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AudioClip(
    val id: String,
    val title: String,
    val durationMs: Long,
    val filePath: String?,
    val createdAt: Long,
    val fileSizeBytes: Long,
    val waveformData: List<Float> = emptyList(),
    val isMock: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
            return sdf.format(Date(createdAt))
        }

    val formattedSize: String
        get() {
            if (fileSizeBytes < 1024) return "${fileSizeBytes} B"
            val kb = fileSizeBytes / 1024.0
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format(Locale.US, "%.1f MB", mb)
        }
}
