package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.RecordingState
import com.example.ui.RecorderUiState
import com.example.ui.components.MinimalistAudioVisualizer
import com.example.ui.metro.MetroTiltBox
import com.example.ui.theme.MetroBlack
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroCyanLight
import com.example.ui.theme.MetroMutedText
import com.example.ui.theme.MetroRed
import com.example.ui.theme.MetroSubtleGray
import com.example.ui.theme.MetroWhite
import java.util.Locale

@Composable
fun RecordScreen(
    state: RecorderUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartRecording()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MetroBlack)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status indicator in clean Metro typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (state.recordingState == RecordingState.RECORDING) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(MetroRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECORDING",
                        color = MetroRed,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                } else if (state.recordingState == RecordingState.PAUSED) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0A30A))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAUSED",
                        color = Color(0xFFF0A30A),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                } else {
                    Text(
                        text = "READY TO RECORD",
                        color = MetroMutedText,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (state.isHighQuality) "48 kHz • 192 kbps AAC" else "44.1 kHz • 96 kbps AAC",
                color = MetroSubtleGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Ultra-Large Live Timer in 64px / Segoe UI Light styling
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            val totalSeconds = state.elapsedMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hundredths = (state.elapsedMs % 1000) / 10

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.testTag("record_timer_display")
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    color = if (state.recordingState == RecordingState.RECORDING) MetroWhite else Color(0xFFDCDCDC),
                    fontSize = 62.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = String.format(Locale.US, ".%02d", hundredths),
                    color = MetroCyan,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )
            }
        }

        // Minimalist Canvas Audio Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("minimalist_visualizer_canvas")
        ) {
            MinimalistAudioVisualizer(
                state = state.recordingState,
                amplitudes = state.amplitudeHistory,
                currentAmplitude = state.liveAmplitude
            )
        }

        // Circular Record / Stop Controls with 3D Tilt
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel button (visible when recording or paused)
            AnimatedVisibility(
                visible = state.recordingState != RecordingState.IDLE,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                MetroTiltBox(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MetroMutedText, CircleShape)
                        .testTag("cancel_record_btn"),
                    onClick = onCancelRecording
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel recording",
                            tint = MetroWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Main Circular Record / Stop Toggle
            MetroTiltBox(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = if (state.recordingState == RecordingState.RECORDING) MetroCyan else MetroWhite,
                        shape = CircleShape
                    )
                    .background(Color(0xFF0F0F0F))
                    .testTag("record_stop_toggle_btn"),
                onClick = {
                    when (state.recordingState) {
                        RecordingState.IDLE -> {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                onStartRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        RecordingState.RECORDING, RecordingState.PAUSED -> {
                            onStopRecording()
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.recordingState == RecordingState.IDLE) {
                        // Iconic Record circle (red dot)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MetroRed)
                        )
                    } else {
                        // Stop square (red square with rounded corners)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MetroRed)
                        )
                    }
                }
            }

            // Pause / Resume button (visible when recording or paused)
            AnimatedVisibility(
                visible = state.recordingState != RecordingState.IDLE,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                MetroTiltBox(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(
                            1.5.dp,
                            if (state.recordingState == RecordingState.PAUSED) MetroCyan else MetroWhite,
                            CircleShape
                        )
                        .testTag("pause_resume_record_btn"),
                    onClick = {
                        if (state.recordingState == RecordingState.RECORDING) {
                            onPauseRecording()
                        } else {
                            onResumeRecording()
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.recordingState == RecordingState.PAUSED) {
                                Icons.Default.PlayArrow
                            } else {
                                Icons.Default.Pause
                            },
                            contentDescription = if (state.recordingState == RecordingState.PAUSED) "Resume" else "Pause",
                            tint = if (state.recordingState == RecordingState.PAUSED) MetroCyan else MetroWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
