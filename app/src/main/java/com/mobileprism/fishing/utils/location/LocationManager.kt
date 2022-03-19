package com.mobileprism.fishing.utils.location

import android.app.Activity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.mobileprism.fishing.ui.home.map.LocationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationManager {
    @OptIn(ExperimentalPermissionsApi::class)
    fun getCurrentLocationFlow(): Flow<LocationState>

    fun checkGPSEnabled(activity: Activity, onGpsEnabled: () -> Unit)
}
