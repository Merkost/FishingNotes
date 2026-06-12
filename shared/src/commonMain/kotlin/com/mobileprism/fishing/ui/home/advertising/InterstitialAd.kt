package com.mobileprism.fishing.ui.home.advertising

import androidx.compose.runtime.Composable

@Composable
expect fun rememberInterstitialAdLauncher(onComplete: () -> Unit): () -> Unit
