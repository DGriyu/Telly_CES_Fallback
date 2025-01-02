package com.example.telly_ces_fallback.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.telly_ces_fallback.ui.theme.OldestText
import com.example.telly_ces_fallback.ui.theme.SecondaryText
import com.example.telly_ces_fallback.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MessageAnimationState(
    val text: String = "",
    val isVisible: Boolean = false,
    val currentStyle: TextStyle
)

@Composable
fun ScrollingTextBox(messages: List<String>) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val lastThreeItems = messages.takeLast(3).reversed()

    // Scroll to the latest message
    LaunchedEffect(messages.size) {
        coroutineScope.launch {
            delay(200) // Ensure the new item is added before scrolling
            listState.animateScrollToItem(index = 0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(lastThreeItems, key = { index, _ -> index }) { i, message ->
            AnimatedMessageItem(i, message)
        }
    }
}

@Composable
fun AnimatedMessageItem(index: Int, message: String) {
    var animationState by remember {
        mutableStateOf(MessageAnimationState(
            currentStyle = getTypographyOfIndex(index)
        ))
    }
    LaunchedEffect(message, index) {

        // Handle typing animation
        launch {
            animationState = animationState.copy(isVisible = true, text = "")
            val typeInterval = 500 / message.length.coerceIn(1..20)

            message.forEachIndexed { _, char ->
                delay(typeInterval.toLong())
                var newText = animationState.text + char
                if (index == 0 && newText.length > 200) {
                    newText = "..." + newText.takeLast(197)
                }
                animationState = animationState.copy(
                    text = newText
                )
            }
        }

        // Handle style transition
        launch {
            animationState = animationState.copy(
                currentStyle = getTypographyOfIndex(index)
            )
        }
    }

    AnimatedVisibility(
        visible = animationState.isVisible,
        enter = fadeIn(animationSpec = tween(600)) +
                slideInHorizontally(
                    initialOffsetX = { -300 }, // Slide in from left
                    animationSpec = tween(600)
                ),
        exit = slideOutVertically(
            targetOffsetY = { -50 }, // Slide up when removed
            animationSpec = tween(durationMillis = 500)
        ) + fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .animateContentSize()
        ) {
            Text(
                text = animationState.text,
                style = animationState.currentStyle,
                maxLines = if (index == 0) 4 else 1,
                overflow = if (index == 0) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

private fun getTypographyOfIndex(index: Int): TextStyle {
    return when (index) {
        0 -> Typography.bodyLarge
        1 -> SecondaryText
        else -> OldestText
    }
}

@Preview(showBackground = true)
@Composable
fun ScrollingTextBoxPreview() {

    ScrollingTextBox(
        messages = listOf(
            "Use precise timing to dodge his attacks.",
            "What are dragons weak to?",
            "Dragons are weak to strike damage, lightning, and fire."
        )
    )
}