package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audio.RecordingState
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroCyanLight
import kotlin.math.sin

@Composable
fun MinimalistAudioVisualizer(
    state: RecordingState,
    amplitudes: List<Float>,
    currentAmplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizerAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val barCount = 38
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

        // Subtle baseline guide
        drawLine(
            color = Color(0x331BA1E2),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        val barBrush = Brush.verticalGradient(
            colors = listOf(
                MetroCyanLight,
                MetroCyan,
                MetroCyan.copy(alpha = 0.8f)
            )
        )

        when (state) {
            RecordingState.RECORDING -> {
                // Live reactive bars based on recent amplitude history + current mic energy
                for (i in 0 until barCount) {
                    val x = i * (barWidth + barSpacing)

                    // Get amplitude from list or synthesize based on current amplitude
                    val historyIndex = amplitudes.size - barCount + i
                    val baseAmp = if (historyIndex in amplitudes.indices) {
                        amplitudes[historyIndex]
                    } else {
                        currentAmplitude * 0.4f
                    }

                    // Add dynamic modulation for rich frequency sensation
                    val sineMod = (sin(phase * 2f + i * 0.35f) * 0.15f).coerceAtLeast(0f)
                    val effectiveAmp = ((baseAmp + sineMod * currentAmplitude) * 1.5f + 0.05f).coerceIn(0.04f, 1.0f)

                    val barHeight = (height * 0.88f * effectiveAmp).coerceAtLeast(4.dp.toPx())
                    val top = centerY - barHeight / 2f

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Tiny peak marker for authentic audio meter look
                    val peakAlpha = (effectiveAmp * 0.9f).coerceIn(0.3f, 1f)
                    drawRect(
                        color = Color.White.copy(alpha = peakAlpha),
                        topLeft = Offset(x, (top - 3.dp.toPx()).coerceAtLeast(0f)),
                        size = Size(barWidth, 1.5.dp.toPx())
                    )
                }
            }
            RecordingState.PAUSED -> {
                // Dimmed frozen bars
                for (i in 0 until barCount) {
                    val x = i * (barWidth + barSpacing)
                    val historyIndex = amplitudes.size - barCount + i
                    val baseAmp = if (historyIndex in amplitudes.indices) {
                        amplitudes[historyIndex]
                    } else 0.12f

                    val barHeight = (height * 0.7f * baseAmp).coerceAtLeast(4.dp.toPx())
                    val top = centerY - barHeight / 2f

                    drawRoundRect(
                        color = MetroCyan.copy(alpha = 0.35f),
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
            RecordingState.IDLE -> {
                // Minimalist gentle undulating idle wave
                for (i in 0 until barCount) {
                    val x = i * (barWidth + barSpacing)
                    val waveFactor = sin(phase + i * 0.22f) * 0.5f + 0.5f
                    val idleHeight = (12.dp.toPx() + waveFactor * 26.dp.toPx())
                    val top = centerY - idleHeight / 2f

                    drawRoundRect(
                        color = MetroCyan.copy(alpha = 0.25f + waveFactor * 0.35f),
                        topLeft = Offset(x, top),
                        size = Size(barWidth, idleHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }
    }
}
