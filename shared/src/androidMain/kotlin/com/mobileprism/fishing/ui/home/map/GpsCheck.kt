package com.mobileprism.fishing.ui.home.map

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mobileprism.fishing.utils.location.LocationManagerImpl
import org.koin.compose.koinInject

@Composable
actual fun rememberGPSChecker(): (onEnabled: () -> Unit, onDisabled: () -> Unit) -> Unit {
    val context = LocalContext.current
    val locationManager: LocationManagerImpl = koinInject()
    return remember(context, locationManager) {
        { onEnabled: () -> Unit, onDisabled: () -> Unit ->
            locationManager.checkGPSEnabled(context as Activity, onEnabled, onDisabled)
        }
    }
}
