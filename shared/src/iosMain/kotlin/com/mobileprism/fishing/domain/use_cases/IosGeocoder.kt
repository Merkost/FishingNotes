package com.mobileprism.fishing.domain.use_cases

class IosGeocoder : PlatformGeocoder {
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = null
}
