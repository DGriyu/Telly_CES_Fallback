package com.example.telly_ces_fallback.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.telly_ces_fallback.ui.theme.OldestText
import com.example.telly_ces_fallback.ui.theme.SecondaryText
import com.example.telly_ces_fallback.ui.theme.Typography
import kotlinx.coroutines.delay

@Composable
fun ScrollingTextBox(messages: List<String>) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        // Adding a slight delay to ensure the new item is laid out
        delay(100)
        listState.animateScrollToItem(index = messages.size - 1)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            items(messages.reversed().take(3).withIndex().toList(), key = { (index, _) -> index }) { (index, message) ->
                val isVisible = index != 3
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    val modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateContentSize()
                        .align(
                            when (index) {
                                0 -> Alignment.CenterStart // Middle item
                                else -> Alignment.TopStart // Above middle
                            }
                        )
                    Text(
                        text = message,
                        style = if (message == messages.last()) {
                            Typography.bodyLarge
                        } else if (message == messages[messages.size - 2]) {
                            SecondaryText
                        } else {
                            OldestText
                        },
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScrollingTextBoxPreview() {

    ScrollingTextBox(messages = listOf("Message 1", "Message 2", "Message 3", "Message 4", "Message 5"))
}