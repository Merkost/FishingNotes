package com.mobileprism.fishing.model.entity.weather

data class WeatherApiForecast(
    val current: Current = Current(),
    val forecast: Forecast = Forecast(),
    val location: Location = Location()
)