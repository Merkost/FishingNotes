package com.mobileprism.fishing.ui.home.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.LottieClipSpec
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.kimplify.cedar.logging.Cedar

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PointerAnimation(pointerState: PointerState, modifier: Modifier = Modifier) {
    val darkTheme = isSystemInDarkTheme()
    val markerResName = if (darkTheme) "marker_night" else "marker"

    var jsonString by remember(markerResName) { mutableStateOf<String?>(null) }
    LaunchedEffect(markerResName) {
        jsonString = Res.readBytes("files/$markerResName.json").decodeToString()
    }

    jsonString?.let { json ->
        val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
        val lottieAnimatable = rememberLottieAnimatable()

        val startMinMaxFrame by remember {
            mutableStateOf(LottieClipSpec.Frame(0, 50))
        }
        val finishMinMaxFrame by remember {
            mutableStateOf(LottieClipSpec.Frame(50, 82))
        }

        LaunchedEffect(pointerState, composition) {
            if (composition == null) return@LaunchedEffect
            Cedar.tag("PointerAnimation").d("pointerState: $pointerState")
            if (pointerState == PointerState.ShowMarker) {
                lottieAnimatable.animate(
                    composition,
                    iteration = 1,
                    initialProgress = 0f,
                    continueFromPreviousAnimate = false,
                    clipSpec = startMinMaxFrame,
                )
            } else {
                lottieAnimatable.animate(
                    composition,
                    iteration = 1,
                    continueFromPreviousAnimate = false,
                    clipSpec = finishMinMaxFrame,
                )
            }
        }

        Image(
            modifier = modifier.size(128.dp),
            painter = rememberLottiePainter(
                composition = composition,
                progress = { lottieAnimatable.progress },
            ),
            contentDescription = "Lottie animation"
        )
    }
}
