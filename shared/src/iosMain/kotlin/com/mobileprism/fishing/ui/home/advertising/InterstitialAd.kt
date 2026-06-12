package com.mobileprism.fishing.ui.home.advertising

import androidx.compose.runtime.Composable

@Composable
actual fun rememberInterstitialAdLauncher(onComplete: () -> Unit): () -> Unit = { onComplete() }
