package com.mobileprism.fishing.domain.use_cases

import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidGeocoder(private val geocoder: Geocoder) : PlatformGeocoder {
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(latitude, longitude, 1) { results ->
                    cont.resume(results)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1).orEmpty()
        }
        return extractPlaceName(addresses.firstOrNull())
    }

    private fun extractPlaceName(address: Address?): String? {
        return address?.let {
            when {
                !it.subAdminArea.isNullOrBlank() -> it.subAdminArea.replaceFirstChar { c -> c.uppercase() }
                !it.adminArea.isNullOrBlank() -> it.adminArea.replaceFirstChar { c -> c.uppercase() }
                else -> null
            }
        }
    }
}
