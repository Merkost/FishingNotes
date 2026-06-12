package com.mobileprism.fishing.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberExitApp(): () -> Unit {
    val context = LocalContext.current
    return {
        (context as? Activity)?.finish()
    }
}
