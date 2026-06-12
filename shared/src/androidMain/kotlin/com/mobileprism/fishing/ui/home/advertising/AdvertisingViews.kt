package com.mobileprism.fishing.ui.home.advertising

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
actual fun BannerAdvertView(
    modifier: Modifier,
    adId: String,
) {
    val adSize = AdSize.FULL_BANNER
    val isInEditMode = LocalInspectionMode.current
    Row(modifier = modifier.fillMaxWidth()) {
        if (isInEditMode) {
            Text(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onError,
                text = stringResource(Res.string.advert_here),
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