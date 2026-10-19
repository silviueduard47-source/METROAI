package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MetroColorScheme = darkColorScheme(
    primary = MetroCyan,
    onPrimary = MetroWhite,
    primaryContainer = MetroCyanDark,
    onPrimaryContainer = MetroWhite,
    secondary = MetroCyanLight,
    onSecondary = MetroBlack,
    background = MetroBlack,
    onBackground = MetroWhite,
    surface = MetroDarkGray,
    onSurface = MetroWhite,
    surfaceVariant = MetroAppBarBg,
    onSurfaceVariant = MetroMutedText,
    outline = MetroSubtleGray,
    outlineVariant = MetroBorder,
    error = MetroRed,
    onError = MetroWhite
)

@Composable
fun MetroVoiceRecorderTheme(
    content: @Composable () -> Unit
) {
    // Windows Phone Metro theme is always pure AMOLED black with cyan accent
    MaterialTheme(
        colorScheme = MetroColorScheme,
        typography = Typography,
        content = content
    )
}
