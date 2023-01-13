package com.mobileprism.fishing.domain.entity.weather

import com.mobileprism.fishing.model.entity.FishingWeather

data class NewCatchWeatherData(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val fishingWeather: FishingWeather = FishingWeather.SUN,
    val temperature: String = "0",
    val windSpeed: String = "0",
    val windDeg: Int = 0,
    val pressure: String = "0",
    val moonPhase: Float = 0f,
)