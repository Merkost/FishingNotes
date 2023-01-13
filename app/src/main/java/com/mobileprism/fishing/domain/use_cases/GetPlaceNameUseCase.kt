package com.mobileprism.fishing.domain.use_cases

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.mobileprism.fishing.ui.home.map.GeocoderResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class GetPlaceNameUseCase(private val geocoder: Geocoder) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
    ) = callbackFlow {
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocation(latitude, longitude, 1) {
                    it.firstOrNull()?.let { address ->
                        trySend(address.getAddressName())
                    }
                }
            } else {
                val position = geocoder.getFromLocation(latitude, longitude, 1)
                position?.firstOrNull()?.let { address ->
                    trySend(address.getAddressName())
                } ?: trySend(GeocoderResult.NoNamePlace)
            }
        } catch (e: Throwable) {
            send(GeocoderResult.Failed)
        }

        awaitClose {}
    }

    private fun Address.getAddressName(): GeocoderResult {
        return if (!this.subAdminArea.isNullOrBlank()) {
            GeocoderResult.Success(this.subAdminArea.replaceFirstChar { it.uppercase() })
        } else if (!this.adminArea.isNullOrBlank()) {
            GeocoderResult.Success(this.adminArea.replaceFirstChar { it.uppercase() })
        } else GeocoderResult.NoNamePlace
    }
}


