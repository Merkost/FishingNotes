package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable

actual fun isDynamicColorSupported(): Boolean = false

@Composable
actual fun rememberAppVersion(): String? = null

@Composable
actual fun rememberOpenAppStore(): () -> Unit = { }

@Composable
actual fun rememberBillingLauncher(): (() -> Unit)? = null
