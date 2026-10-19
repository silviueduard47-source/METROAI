package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.AudioClip
import com.example.ui.PlaybackState
import com.example.ui.RecorderUiState
import com.example.ui.metro.MetroTiltBox
import com.example.ui.theme.MetroBlack
import com.example.ui.theme.MetroBorder
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroCyanLight
import com.example.ui.theme.MetroDarkGray
import com.example.ui.theme.MetroMutedText
import com.example.ui.theme.MetroRed
import com.example.ui.theme.MetroSubtleGray
import com.example.ui.theme.MetroWhite
import java.io.File
import java.util.Locale

@Composable
fun SavedClipsScreen(
    state: RecorderUiState,
    onPlayClip: (AudioClip) -> Unit,
    onSeek: (Float) -> Unit,
    onRequestDelete: (AudioClip) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredClips = remember(state.clips, searchQuery) {
        if (searchQuery.isBlank()) {
            state.clips
        } else {
            state.clips.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MetroBlack)
    ) {
        // Search & Count Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${filteredClips.size} CLIPS",
                color = MetroMutedText,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            // Minimalist Metro Search Field
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "search clips…",
                        color = MetroMutedText,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MetroMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MetroDarkGray,
                    unfocusedContainerColor = Color(0xFF101010),
                    focusedTextColor = MetroWhite,
                    unfocusedTextColor = MetroWhite,
                    focusedIndicatorColor = MetroCyan,
                    unfocusedIndicatorColor = MetroBorder
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(44.dp)
                    .testTag("search_clips_input")
            )
        }

        if (filteredClips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isBlank()) "no saved clips" else "no matching clips",
                        color = MetroMutedText,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "swipe to record to capture a new memo",
                        color = MetroSubtleGray,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredClips,
                    key = { it.id }
                ) { clip ->
                    val isPlayingThis = state.playback.clipId == clip.id && state.playback.isPlaying
                    val isPausedThis = state.playback.clipId == clip.id && state.playback.isPaused

                    AudioClipItemCard(
                        clip = clip,
                        isPlaying = isPlayingThis,
                        isPaused = isPausedThis,
                        playbackState = state.playback,
                        onPlayClick = { onPlayClip(clip) },
                        onSeek = onSeek,
                        onDeleteClick = { onRequestDelete(clip) },
                        onShareClick = {
                            if (clip.filePath != null) {
                                val file = File(clip.filePath)
                                if (file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share audio clip"))
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (state.clipToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            containerColor = Color(0xFF181818),
            title = {
                Text(
                    text = "delete clip?",
                    color = MetroWhite,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${state.clipToDelete.title}'?",
                    color = MetroMutedText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MetroRed)
                ) {
                    Text("delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MetroWhite)
                ) {
                    Text("cancel")
                }
            }
        )
    }
}

@Composable
fun AudioClipItemCard(
    clip: AudioClip,
    isPlaying: Boolean,
    isPaused: Boolean,
    playbackState: PlaybackState,
    onPlayClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    MetroTiltBox(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp)) // Metro strictly prefers crisp geometric edges
            .border(
                width = 1.dp,
                color = if (isPlaying) MetroCyan else MetroBorder
            )
            .background(Color(0xFF101010))
            .testTag("clip_item_${clip.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clip.title,
                        color = MetroWhite,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = clip.formattedDuration,
                            color = MetroCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "  •  ${clip.formattedDate}  •  ${clip.formattedSize}",
                            color = MetroMutedText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                // Inline Play / Pause Round Outlined Button
                MetroTiltBox(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (isPlaying) MetroCyan else MetroWhite,
                            shape = CircleShape
                        )
                        .background(if (isPlaying) MetroCyan.copy(alpha = 0.15f) else Color.Transparent)
                        .testTag("play_pause_btn_${clip.id}"),
                    onClick = onPlayClick
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) MetroCyan else MetroWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini Waveform Visualizer Preview
            MiniWaveformPreview(
                waveform = clip.waveformData,
                progress = if (isPlaying || isPaused) playbackState.progressFraction else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )

            // Playback progress bar when actively playing or paused
            AnimatedVisibility(
                visible = isPlaying || isPaused,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Slider(
                        value = playbackState.progressFraction,
                        onValueChange = onSeek,
                        colors = SliderDefaults.colors(
                            thumbColor = MetroCyan,
                            activeTrackColor = MetroCyan,
                            inactiveTrackColor = MetroSubtleGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentSec = playbackState.currentMs / 1000
                        val totalSec = clip.durationMs / 1000
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", currentSec / 60, currentSec % 60),
                            color = MetroCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", totalSec / 60, totalSec % 60),
                            color = MetroMutedText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Actions Row (Delete & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (clip.filePath != null) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(34.dp).testTag("share_btn_${clip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share clip",
                            tint = MetroMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(34.dp).testTag("delete_btn_${clip.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete clip",
                        tint = MetroMutedText,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniWaveformPreview(
    waveform: List<Float>,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val bars = if (waveform.isNotEmpty()) waveform else List(32) { 0.3f }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val barCount = bars.size
        val barSpacing = 2.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(1.5.dp.toPx())

        for (i in 0 until barCount) {
            val x = i * (barWidth + barSpacing)
            val barFrac = (i.toFloat() / barCount)
            val isPlayed = barFrac <= progress

            val amp = bars[i].coerceIn(0.12f, 1f)
            val barHeight = (height * 0.9f * amp).coerceAtLeast(2.dp.toPx())
            val top = centerY - barHeight / 2f

            drawRoundRect(
                color = if (isPlayed) MetroCyanLight else MetroSubtleGray,
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}
