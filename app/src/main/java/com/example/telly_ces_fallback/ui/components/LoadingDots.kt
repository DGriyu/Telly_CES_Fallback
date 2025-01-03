package com.example.telly_ces_fallback.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.telly_ces_fallback.R

@Composable
fun LoadingDots() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.jumping_dots_load_anim)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.width(204.dp).height(48.dp)
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }
}

@Preview
@Composable
fun LoadingDotsPreview() {
    Box {
        LoadingDots()
    }
}
