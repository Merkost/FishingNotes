package com.mobileprism.fishing.ui.home.advertising

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun rememberPrivacyOptionsLauncher(): (() -> Unit)? {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return null
    val isRequired by AdsConsentManager.privacyOptionsRequired.collectAsState()
    return if (isRequired) {
        { AdsConsentManager.showPrivacyOptionsForm(activity) }
    } else {
        null
    }
}
