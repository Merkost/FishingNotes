package com.mobileprism.fishing.ui.home.advertising

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mobileprism.fishing.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdvertView(
    modifier: Modifier = Modifier,
    adId: String,
    adSize: AdSize = AdSize.FULL_BANNER
) {
    val isInEditMode = LocalInspectionMode.current
    Row(modifier = modifier.fillMaxWidth()) {
        if (isInEditMode) {
            Text(
                modifier = modifier
                    .background(Color.Red)
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
                text = stringResource(id = R.string.advert_here),
            )
        } else {
            AndroidView(
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(adSize)
                        adUnitId = adId
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}