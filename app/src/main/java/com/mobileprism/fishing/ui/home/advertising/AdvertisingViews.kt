package com.mobileprism.fishing.ui.home.advertising

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize
import com.google.android.gms.ads.AdView
import com.mobileprism.fishing.utils.network.currentConnectivityState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow

@Composable
fun AdaptiveBannerAdvertView(modifier: Modifier = Modifier, adId: String) {
    val configuration = LocalConfiguration.current
    val isInEditMode = LocalInspectionMode.current
    val localContext = LocalContext.current

    val connectionState by localContext.observeConnectivityAsFlow()
        .collectAsState(initial = localContext.currentConnectivityState)

    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context,
                            configuration.screenWidthDp
                        )
                    )
                    adUnitId = adId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun BannerAdvertView(modifier: Modifier = Modifier, adId: String, padding: Dp) {
    val isInEditMode = LocalInspectionMode.current
    val localContext = LocalContext.current

    var size by remember { mutableStateOf(IntSize.Zero) }

    val connectionState by localContext.observeConnectivityAsFlow()
        .collectAsState(initial = localContext.currentConnectivityState)

    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
        ) {
            val configuration = LocalConfiguration.current

            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { context ->
                    AdView(context).apply {
                        /* adSize = AdSize
                             .getCurrentOrientationAnchoredAdaptiveBannerAdSize(context,
                                 configuration.screenWidthDp-padding.value.toInt()*2)*/
                        // FIXME: Fix AdView
                        adUnitId = adId
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}