package com.mobileprism.fishing.domain.use_cases

interface PlatformGeocoder {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String?
}
