package com.mobileprism.fishing.utils.location

import com.mobileprism.fishing.ui.home.map.LocationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class IosLocationManager : LocationManager {
    override fun getCurrentLocationFlow(): Flow<LocationState> =
        flowOf(LocationState.NoPermission)
}
