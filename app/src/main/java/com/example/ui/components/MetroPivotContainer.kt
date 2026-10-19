package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.metro.MetroTiltBox
import com.example.ui.theme.MetroBlack
import com.example.ui.theme.MetroCyan
import com.example.ui.theme.MetroMutedText
import com.example.ui.theme.MetroWhite

@Composable
fun MetroPivotContainer(
    currentPivotIndex: Int,
    onPivotChange: (Int) -> Unit,
    recordContent: @Composable () -> Unit,
    savedClipsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MetroBlack)
            .statusBarsPadding()
    ) {
        // Iconic Windows Phone Metro Header: 'sound recorder' in 64px Segoe UI Light
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp)
        ) {
            Text(
                text = "sound recorder",
                color = MetroWhite,
                fontSize = 44.sp, // ~64px at typical density, large light display
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp,
                modifier = Modifier.testTag("app_metro_header")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pivot Tabs Header: 'record' and 'saved clips'
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 'record' tab
                MetroTiltBox(
                    modifier = Modifier.testTag("pivot_tab_record"),
                    onClick = { onPivotChange(0) }
                ) {
                    Column {
                        Text(
                            text = "record",
                            color = if (currentPivotIndex == 0) MetroWhite else Color(0x66FFFFFF),
                            fontSize = if (currentPivotIndex == 0) 28.sp else 24.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (currentPivotIndex == 0) FontWeight.Normal else FontWeight.Light,
                            letterSpacing = (-0.5).sp
                        )
                        if (currentPivotIndex == 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .height(2.5.dp)
                                    .fillMaxWidth(0.18f)
                                    .background(MetroCyan)
                            )
                        }
                    }
                }

                // 'saved clips' tab
                MetroTiltBox(
                    modifier = Modifier.testTag("pivot_tab_saved_clips"),
                    onClick = { onPivotChange(1) }
                ) {
                    Column {
                        Text(
                            text = "saved clips",
                            color = if (currentPivotIndex == 1) MetroWhite else Color(0x66FFFFFF),
                            fontSize = if (currentPivotIndex == 1) 28.sp else 24.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (currentPivotIndex == 1) FontWeight.Normal else FontWeight.Light,
                            letterSpacing = (-0.5).sp
                        )
                        if (currentPivotIndex == 1) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .height(2.5.dp)
                                    .fillMaxWidth(0.24f)
                                    .background(MetroCyan)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Screen content with smooth animated Metro transition
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = currentPivotIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(280)) { width -> width } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { width -> -width / 2 } + fadeOut(animationSpec = tween(200)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(280)) { width -> -width } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { width -> width / 2 } + fadeOut(animationSpec = tween(200)))
                    }
                },
                label = "pivotContentTransition"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> recordContent()
                    1 -> savedClipsContent()
                }
            }
        }
    }
}
