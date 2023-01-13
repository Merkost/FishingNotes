package com.mobileprism.fishing.domain.entity.raw

import com.mobileprism.fishing.model.entity.FishingWeather

data class NewCatchWeather(
    var fishingWeather: FishingWeather = FishingWeather.SUN,
    var temperatureInC: Int = 0,
    var pressureInMmhg: Int = 0,
    var windInMs: Int = 0,
    var windDirInDeg: Float = 0.0f,
    var moonPhase: Float = 0f
)
