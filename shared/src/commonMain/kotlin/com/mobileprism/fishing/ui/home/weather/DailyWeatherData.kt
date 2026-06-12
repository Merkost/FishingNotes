package com.mobileprism.fishing.ui.home.weather

import com.mobileprism.fishing.domain.entity.weather.Daily
import kotlinx.serialization.Serializable

@Serializable
data class DailyWeatherData(
    val selectedDay: Int,
    val dailyForecast: List<Daily>
)
