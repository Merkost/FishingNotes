package com.mobileprism.fishing.utils.location

import android.app.Activity
import com.mobileprism.fishing.ui.home.map.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationManager {
    fun getCurrentLocationFlow(): Flow<LocationState>

    fun checkGPSEnabled(activity: Activity, onGpsEnabled: () -> Unit)
}
