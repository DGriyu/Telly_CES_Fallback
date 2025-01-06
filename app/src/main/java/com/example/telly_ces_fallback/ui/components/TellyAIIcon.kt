package com.example.telly_ces_fallback.ui.components

import androidx.annotation.RawRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.telly_ces_fallback.R
import com.example.telly_ces_fallback.viewmodel.AIHomeState

@Composable
fun TellyAIIcon(
    uiState: AIHomeState,
    modifier: Modifier = Modifier.width(199.dp).height(199.dp),
    contentDescription: String? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Crossfade(
            targetState = uiState,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        ) { state ->
            when (state) {
                is AIHomeState.Error -> {
                }
                AIHomeState.Loading, AIHomeState.Loaded -> LaunchLogoLottieAnimation( R.raw.telly_idle_anim, modifier)
                AIHomeState.Launching -> LaunchLogoLottieAnimation( R.raw.telly_launch_anim, modifier)
            }
        }
    }
}

@Composable
fun LaunchLogoLottieAnimation(@RawRes rawRes: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(rawRes)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TellyAIIconPreview() {

    TellyAIIcon(AIHomeState.Loading)
}