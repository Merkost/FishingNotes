package com.mobileprism.fishing.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import fishing.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AnimatedResource(
    resName: String,
    modifier: Modifier = Modifier,
    iterations: Int = Int.MAX_VALUE,
    contentScale: ContentScale = ContentScale.Fit,
) {
    var jsonString by remember(resName) { mutableStateOf<String?>(null) }
    LaunchedEffect(resName) {
        jsonString = Res.readBytes("files/$resName.json").decodeToString()
    }
    jsonString?.let { json ->
        val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
        val progress by animateLottieCompositionAsState(composition, iterations = iterations)
        Image(
            modifier = modifier, contentScale = contentScale,
            painter = rememberLottiePainter(
                composition = composition,
                progress = { progress },
            ),
            contentDescription = "Lottie animation"
        )
    }
}
