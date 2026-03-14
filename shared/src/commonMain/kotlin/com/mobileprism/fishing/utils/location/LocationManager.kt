package com.mobileprism.fishing.utils.location

import com.mobileprism.fishing.ui.home.map.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationManager {
    fun getCurrentLocationFlow(): Flow<LocationState>
}
