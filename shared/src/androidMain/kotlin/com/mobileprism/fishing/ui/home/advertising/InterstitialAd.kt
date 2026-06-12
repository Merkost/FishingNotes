package com.mobileprism.fishing.ui.home.advertising

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberInterstitialAdLauncher(onComplete: () -> Unit): () -> Unit {
    val context = LocalContext.current
    return remember(context, onComplete) {
        { showInterstitialAd(context = context, onAdLoaded = onComplete) }
    }
}
