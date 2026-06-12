package com.mobileprism.fishing.domain.entity.raw

import androidx.compose.runtime.Immutable

@Immutable
data class NewCatchWeather(
    val weatherDescription: String = "",
    val icon: String = "",
    val temperatureInC: Int = 0,
    val pressureInMmhg: Int = 0,
    val windInMs: Int = 0,
    val windDirInDeg: Float = 0.0f,
    val moonPhase: Float = 0f
)
