package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.alexzhirkevich.compottie.LottieAnimation
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import fishing.shared.generated.resources.Res

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
        val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(json))
        val lottieIterations = if (iterations == Int.MAX_VALUE) LottieConstants.IterateForever else iterations
        val progress by animateLottieCompositionAsState(composition, iterations = lottieIterations)
        LottieAnimation(composition, { progress }, modifier = modifier, contentScale = contentScale)
    }
}
