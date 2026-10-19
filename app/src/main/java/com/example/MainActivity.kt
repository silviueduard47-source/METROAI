package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VoiceRecorderViewModel
import com.example.ui.components.MetroAboutDialog
import com.example.ui.components.MetroBottomAppBar
import com.example.ui.components.MetroPivotContainer
import com.example.ui.screens.RecordScreen
import com.example.ui.screens.SavedClipsScreen
import com.example.ui.theme.MetroBlack
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroVoiceRecorderTheme
import com.example.ui.theme.MetroWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetroVoiceRecorderTheme {
                SoundRecorderApp()
            }
        }
    }
}

@Composable
fun SoundRecorderApp(
    viewModel: VoiceRecorderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle back button: if on saved clips, back navigates to record
    BackHandler(enabled = state.pivotIndex != 0) {
        viewModel.setPivot(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MetroBlack)
    ) {
        Scaffold(
            containerColor = MetroBlack,
            bottomBar = {
                MetroBottomAppBar(
                    currentPivotIndex = state.pivotIndex,
                    onNavigatePivot = { viewModel.setPivot(it) },
                    isHighQuality = state.isHighQuality,
                    onToggleQuality = { viewModel.toggleHighQuality() },
                    onResetDemoClips = { viewModel.restoreDemoClips() },
                    onShowAbout = { viewModel.setShowAboutDialog(true) }
                )
            }
        ) { paddingValues ->
            MetroPivotContainer(
                currentPivotIndex = state.pivotIndex,
                onPivotChange = { viewModel.setPivot(it) },
                recordContent = {
                    RecordScreen(
                        state = state,
                        onStartRecording = { viewModel.startRecording(context) },
                        onStopRecording = { viewModel.stopRecording() },
                        onPauseRecording = { viewModel.pauseRecording() },
                        onResumeRecording = { viewModel.resumeRecording() },
                        onCancelRecording = { viewModel.cancelRecording() }
                    )
                },
                savedClipsContent = {
                    SavedClipsScreen(
                        state = state,
                        onPlayClip = { clip -> viewModel.playClip(context, clip) },
                        onSeek = { fraction -> viewModel.seekPlayback(fraction) },
                        onRequestDelete = { clip -> viewModel.requestDeleteClip(clip) },
                        onConfirmDelete = { viewModel.confirmDeleteClip() },
                        onDismissDelete = { viewModel.requestDeleteClip(null) }
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }

        // Metro Top Toast Notification Banner (Windows Phone Style)
        AnimatedVisibility(
            visible = state.toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            state.toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2800)
                    viewModel.clearToastMessage()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(MetroCyan)
                        .clickable { viewModel.clearToastMessage() }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("metro_toast_banner")
                ) {
                    Text(
                        text = msg,
                        color = MetroBlack,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // About Dialog
        if (state.showAboutDialog) {
            MetroAboutDialog(
                onDismiss = { viewModel.setShowAboutDialog(false) }
            )
        }
    }
}
