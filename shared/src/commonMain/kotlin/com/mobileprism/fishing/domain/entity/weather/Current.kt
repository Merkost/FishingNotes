package com.mobileprism.fishing.domain.entity.weather

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Current(
    @SerialName("dt") val date: Long = 0,
    @SerialName("sunrise") val sunrise: Long = 0,
    @SerialName("sunset") val sunset: Long = 0,
    @SerialName("temp") val temperature: Float = 0.0f,
    @SerialName("pressure") val pressure: Int = 0,
    @SerialName("humidity") val humidity: Int = 0,
    @SerialName("wind_speed") val windSpeed: Float = 0.0f,
    @SerialName("wind_deg") val windDeg: Int = 0,
    @SerialName("weather") val weather: List<Weather> = listOf(Weather())
)
