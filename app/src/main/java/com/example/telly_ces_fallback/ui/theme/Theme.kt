package com.example.telly_ces_fallback.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

@Composable
fun Telly_CES_FallbackTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        background = Color.Transparent,
        primary = CurrentText,
        secondary = DropShadow,
        tertiary = LostConnection
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}