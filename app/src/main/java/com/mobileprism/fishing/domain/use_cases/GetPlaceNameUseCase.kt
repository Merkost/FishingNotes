package com.mobileprism.fishing.domain.use_cases

import android.location.Geocoder
import com.mobileprism.fishing.ui.home.map.GeocoderResult
import kotlinx.coroutines.flow.flow

class GetPlaceNameUseCase(private val geocoder: Geocoder) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
    ) = flow {

        try {
            val position = geocoder.getFromLocation(latitude, longitude, 1)
            position?.first()?.let { address ->
                if (!address.subAdminArea.isNullOrBlank()) {
                   emit(GeocoderResult.Success( address.subAdminArea.replaceFirstChar { it.uppercase() }))
                } else if (!address.adminArea.isNullOrBlank()) {
                    emit(GeocoderResult.Success(address.adminArea.replaceFirstChar { it.uppercase() }))
                } else emit(GeocoderResult.NoNamePlace)
            }
        } catch (e: Throwable) {
            emit(GeocoderResult.Failed)
        }
    }
}
