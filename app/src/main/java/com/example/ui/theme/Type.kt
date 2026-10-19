package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Segoe UI Light style typography for Windows Phone Metro aesthetic
val MetroFontFamily = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 58.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp,
        color = MetroWhite
    ),
    displayMedium = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp,
        color = MetroWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.2).sp,
        color = MetroWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        color = MetroWhite
    ),
    titleLarge = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        color = MetroWhite
    ),
    titleMedium = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = MetroWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
        color = MetroWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
        color = MetroMutedText
    ),
    labelSmall = TextStyle(
        fontFamily = MetroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
        color = MetroWhite
    )
)
