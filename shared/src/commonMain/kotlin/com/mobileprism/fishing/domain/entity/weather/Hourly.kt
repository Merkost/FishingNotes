package com.mobileprism.fishing.domain.entity.weather

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Hourly(
    @SerialName("dt") val date: Long = 0,
    @SerialName("temp") val temperature: Float = 0f,
    @SerialName("pressure") val pressure: Int = 0,
    @SerialName("humidity") val humidity: Int = 0,
    @SerialName("clouds") val clouds: Int = 0,
    @SerialName("wind_speed") val windSpeed: Float = 0f,
    @SerialName("wind_deg") val windDeg: Int = 0,
    @SerialName("weather") val weather: List<Weather> = listOf(Weather()),
    @SerialName("pop") val probabilityOfPrecipitation: Float = 0f,
)
