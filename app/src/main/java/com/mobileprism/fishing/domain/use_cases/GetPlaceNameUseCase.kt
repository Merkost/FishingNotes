package com.mobileprism.fishing.domain.use_cases

import android.location.Geocoder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.text.SimpleDateFormat
import java.util.*

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
                } else emit(GeocoderResult.UnnamedPlace)
            }
        } catch (e: Throwable) {
            emit(GeocoderResult.Failed)
        }
    }
}

sealed class GeocoderResult {
    class Success(val placeName: String): GeocoderResult()
    object UnnamedPlace: GeocoderResult()
    object Failed: GeocoderResult()
}
