package com.mobileprism.fishing.ui.home.map

import androidx.compose.runtime.Composable

@Composable
actual fun rememberGPSChecker(): (onEnabled: () -> Unit) -> Unit = { it() }
