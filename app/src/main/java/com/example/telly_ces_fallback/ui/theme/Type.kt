package com.example.telly_ces_fallback.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        color = CurrentText,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 31.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.5.sp,
        shadow = Shadow(
            color = DropShadow,
            offset = Offset(0f, 0f),
            blurRadius = 21.81f
        )
    )
)

val SecondaryText = TextStyle(
    color = CurrentText,
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 20.96.sp,
    lineHeight = 34.61.sp
)

val OldestText = TextStyle(
    color = CurrentText.copy(alpha = 0.40f),
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 18.4.sp,
    lineHeight = 31.2.sp
)
