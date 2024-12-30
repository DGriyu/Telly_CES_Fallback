package com.example.telly_ces_fallback.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.telly_ces_fallback.ui.components.LoadingDots
import com.example.telly_ces_fallback.ui.components.ScrollingTextBox
import com.example.telly_ces_fallback.ui.components.TellyAIIcon
import com.example.telly_ces_fallback.ui.theme.SurfaceOverlay
import com.example.telly_ces_fallback.ui.theme.Telly_CES_FallbackTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AIHomeScreen(conversationList: StateFlow<List<String>>) {
    val conversation by conversationList.collectAsState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceOverlay
    ) {
        Telly_CES_FallbackTheme {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 65.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TellyAIIcon()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(start = 72.dp, end = 72.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    ScrollingTextBox(messages = conversation)
                    LoadingDots()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AIHomeScreenPreview() {
    val sampleMessages = MutableStateFlow(
        listOf(
            "Use precise timing to dodge his attacks, counter with strong, rapid strikes, and exploit his openings to deplete his health quickly.",
            "What are dragons weak to?",
            "In Elden Ring, dragons are weak to strike damage (hammers, clubs), lightning, and fire. Use these for an edge in battle against them. Want more details?"
        )
    )
    AIHomeScreen(sampleMessages)
}