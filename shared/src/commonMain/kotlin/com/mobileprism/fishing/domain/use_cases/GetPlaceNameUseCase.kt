package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.ui.home.map.GeocoderResult
import kotlinx.coroutines.flow.flow

class GetPlaceNameUseCase(private val geocoder: PlatformGeocoder) : PlaceNameResolver {

    override suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
    ) = flow {
        try {
            val name = geocoder.reverseGeocode(latitude, longitude)
            if (name != null) {
                emit(GeocoderResult.Success(name))
            } else {
                emit(GeocoderResult.NoNamePlace)
            }
        } catch (e: Throwable) {
            emit(GeocoderResult.Failed)
        }
    }
}
