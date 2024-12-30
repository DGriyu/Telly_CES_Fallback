package com.example.telly_ces_fallback.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.telly_ces_fallback.R

@Composable
fun TellyAIIcon() {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context).components {
            add(ImageDecoderDecoder.Factory())
        }.build()

    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.ic_telly_load).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier.width(199.dp).height(199.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun TellyAIIconPreview() {
    TellyAIIcon()
}