package com.example.telly_ces_fallback.ui.screens

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.telly_ces_fallback.ui.components.LoadingDots
import com.example.telly_ces_fallback.ui.components.ScrollingTextBox
import com.example.telly_ces_fallback.ui.components.TellyAIIcon
import com.example.telly_ces_fallback.ui.theme.DropShadow
import com.example.telly_ces_fallback.ui.theme.LostConnection
import com.example.telly_ces_fallback.ui.theme.SurfaceOverlay
import com.example.telly_ces_fallback.ui.theme.Telly_CES_FallbackTheme
import com.example.telly_ces_fallback.viewmodel.AIHomeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AIHomeScreen(conversationList: StateFlow<List<String>>, uiState: StateFlow<AIHomeState>) {
    val conversation by conversationList.collectAsState()
    val ui by uiState.collectAsState()
    Log.d("AIHomeScreen", "UI State: $ui")
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
                    TellyAIIcon(ui)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(start = 72.dp, end = 72.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Crossfade(
                        targetState = ui,
                        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
                    ) { state ->
                        when (state) {
                            is AIHomeState.Error -> Text(text = "Error: ${state.message}", color = LostConnection, modifier = Modifier.align(Alignment.Center))
                            is AIHomeState.Launching, is AIHomeState.Loading -> LoadingDots()
                            is AIHomeState.Loading -> LoadingDots()
                            is AIHomeState.Loaded -> ScrollingTextBox(messages = conversation)
                        }
                    }
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
    AIHomeScreen(sampleMessages, MutableStateFlow(AIHomeState.Loaded))
}

@Preview(showBackground = true, heightDp = 300)
@Composable
fun ScrollingTextBoxPreview() {
    val scope = rememberCoroutineScope()
    val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    // Simulated message generator with realistic patterns
    class MessageSimulator {
        private val baseMessages = listOf(
            "Initializing audio pipeline...",
            "Processing voice input stream...",
            "Analyzing speech patterns...",
            "Generating response matrix...",
            "Optimizing output buffer...",
            "Calculating confidence scores...",
            "Evaluating semantic context..."
        )

        private var lastIndex = 0

        fun nextMessage(): String {
            lastIndex += 1
            return baseMessages[lastIndex]
        }

        fun generateDelay(): Long = when (Random.nextInt(3)) {
            0 -> 800L  // Fast updates
            1 -> 1500L // Medium pause
            else -> 2500L // Longer think time
        }
    }

    val simulator = remember { MessageSimulator() }

    // Start message simulation
    LaunchedEffect(Unit) {
        scope.launch {
            for  (i in 0..5) {
                _messages.value = (_messages.value + simulator.nextMessage()).takeLast(5)
                    .takeLast(5) // Keep last 5 messages for preview
                delay(simulator.generateDelay())
            }
        }
    }

    AIHomeScreen(messages, MutableStateFlow(AIHomeState.Loaded))
}