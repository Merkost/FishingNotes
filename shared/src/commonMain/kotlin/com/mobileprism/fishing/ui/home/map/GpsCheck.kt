package com.mobileprism.fishing.ui.home.map

import androidx.compose.runtime.Composable

@Composable
expect fun rememberGPSChecker(): (onEnabled: () -> Unit) -> Unit
