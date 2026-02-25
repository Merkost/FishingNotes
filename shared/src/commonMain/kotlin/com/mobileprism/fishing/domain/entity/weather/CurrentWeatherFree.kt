package com.mobileprism.fishing.domain.entity.weather

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CurrentWeatherFree(
    val cloud_pct: Int,
    val feels_like: Int,
    val humidity: Int,
    val max_temp: Int,
    val min_temp: Int,
    val sunrise: Int,
    val sunset: Int,
    val temp: Int,
    val wind_degrees: Int,
    val wind_speed: Double
)
