package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable

expect fun isDynamicColorSupported(): Boolean

@Composable
expect fun rememberAppVersion(): String?

@Composable
expect fun rememberOpenAppStore(): () -> Unit

@Composable
expect fun rememberBillingLauncher(): (() -> Unit)?
