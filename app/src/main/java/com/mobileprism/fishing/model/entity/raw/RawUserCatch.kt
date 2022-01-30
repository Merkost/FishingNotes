package com.mobileprism.fishing.model.entity.raw

import android.net.Uri

data class RawUserCatch(
    var description: String? = "",
    var date: Long = 0,
    var fishType: String = "",
    var fishAmount: Int? = 0,
    var fishWeight: Double? = 0.0,
    var fishingRodType: String? = "",
    var fishingBait: String? = "",
    var fishingLure: String? = "",
    var placeTitle: String = "",
    var markerId: String = "",
    var isPublic: Boolean = false,
    var includeWeather: Boolean = true,
    var photos: List<Uri> = listOf(),
    var weatherPrimary: String = "",
    var weatherIcon: String = "",
    var weatherTemperature: Float = 0.0f,
    var weatherWindSpeed: Float = 0.0f,
    var weatherWindDeg: Int = 0,
    var weatherPressure: Int = 0,
    var weatherMoonPhase: Float = 0.0f
)