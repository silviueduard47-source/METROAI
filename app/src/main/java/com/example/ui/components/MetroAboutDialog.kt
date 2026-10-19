package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.metro.MetroTiltBox
import com.example.ui.theme.MetroBorder
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroDarkGray
import com.example.ui.theme.MetroMutedText
import com.example.ui.theme.MetroWhite

@Composable
fun MetroAboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141414),
        modifier = Modifier.testTag("metro_about_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MetroCyan, CircleShape)
                        .background(MetroCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MetroCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "sound recorder",
                        color = MetroWhite,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "Windows Phone Metro Edition",
                        color = MetroCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "A tribute to the iconic Windows Phone Metro Design Language:",
                    color = MetroWhite,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(10.dp))

                AboutBullet("Pure Black (#000000) AMOLED canvas & Cyan (#1BA1E2) accent")
                AboutBullet("Segoe UI Light typography with ultra-large live timer")
                AboutBullet("Minimalist animated audio visualizer canvas")
                AboutBullet("Signature Metro 3D click tilt tactile feedback")
                AboutBullet("Horizontal Pivot navigation: record & saved clips")
                AboutBullet("Bottom App Bar with round outlined buttons")
                AboutBullet("Mock & real audio recording with inline playback")
            }
        },
        confirmButton = {
            MetroTiltBox(onClick = onDismiss) {
                Text(
                    text = "close",
                    color = MetroCyan,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    )
}

@Composable
private fun AboutBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = MetroCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = MetroMutedText,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}
