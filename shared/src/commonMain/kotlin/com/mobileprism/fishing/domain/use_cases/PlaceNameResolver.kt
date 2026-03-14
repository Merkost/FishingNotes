package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.ui.home.map.GeocoderResult
import kotlinx.coroutines.flow.Flow

interface PlaceNameResolver {
    suspend fun invoke(latitude: Double, longitude: Double): Flow<GeocoderResult>
}
