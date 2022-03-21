package com.mobileprism.fishing.domain.entity.raw

data class NewCatchWeather(
    var weatherDescription: String = "",
    var icon: String = "",
    var temperatureInC: Int = 0,
    var pressureInMmhg: Int = 0,
    var windInMs: Int = 0,
    var windDirInDeg: Float = 0.0f,
    var moonPhase: Float = 0f
)
