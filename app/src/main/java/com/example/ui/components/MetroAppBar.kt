package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.metro.MetroTiltBox
import com.example.ui.theme.MetroAppBarBg
import com.example.ui.theme.MetroBorder
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroMutedText
import com.example.ui.theme.MetroWhite

data class MetroAppBarAction(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun MetroBottomAppBar(
    currentPivotIndex: Int,
    onNavigatePivot: (Int) -> Unit,
    isHighQuality: Boolean,
    onToggleQuality: () -> Unit,
    onResetDemoClips: () -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MetroAppBarBg)
            .navigationBarsPadding()
    ) {
        // Thin top divider
        HorizontalDivider(
            color = MetroBorder,
            thickness = 1.dp
        )

        // Expanded drawer menu (Windows Phone style)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161616))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "sound recorder options",
                    color = MetroMutedText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                MetroDrawerItem(
                    title = "audio quality: " + if (isHighQuality) "high (192 kbps)" else "standard (96 kbps)",
                    onClick = {
                        onToggleQuality()
                        isExpanded = false
                    }
                )

                MetroDrawerItem(
                    title = "restore demo mock clips",
                    onClick = {
                        onResetDemoClips()
                        isExpanded = false
                    }
                )

                MetroDrawerItem(
                    title = "about sound recorder",
                    onClick = {
                        onShowAbout()
                        isExpanded = false
                    }
                )
            }
        }

        // Main App Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Record pivot button
                MetroAppBarCircleButton(
                    icon = Icons.Default.Mic,
                    label = "record",
                    testTag = "appbar_record_btn",
                    isSelected = currentPivotIndex == 0,
                    onClick = { onNavigatePivot(0) }
                )

                // Clips pivot button
                MetroAppBarCircleButton(
                    icon = Icons.Default.GraphicEq,
                    label = "clips",
                    testTag = "appbar_clips_btn",
                    isSelected = currentPivotIndex == 1,
                    onClick = { onNavigatePivot(1) }
                )

                // Quality toggle button
                MetroAppBarCircleButton(
                    icon = Icons.Default.HighQuality,
                    label = if (isHighQuality) "high" else "std",
                    testTag = "appbar_quality_btn",
                    isSelected = isHighQuality,
                    onClick = onToggleQuality
                )

                // About / Info button
                MetroAppBarCircleButton(
                    icon = Icons.Default.Info,
                    label = "about",
                    testTag = "appbar_info_btn",
                    isSelected = false,
                    onClick = onShowAbout
                )
            }

            // Metro 3-dots expander
            MetroTiltBox(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .testTag("appbar_more_btn"),
                onClick = { isExpanded = !isExpanded }
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More Options",
                        tint = if (isExpanded) MetroCyan else MetroWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetroAppBarCircleButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    MetroTiltBox(
        modifier = Modifier.testTag(testTag),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) MetroCyan else MetroWhite,
                        shape = CircleShape
                    )
                    .background(if (isSelected) MetroCyan.copy(alpha = 0.15f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) MetroCyan else MetroWhite,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) MetroCyan else MetroMutedText
            )
        }
    }
}

@Composable
private fun MetroDrawerItem(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = MetroWhite,
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light
        )
    }
}
