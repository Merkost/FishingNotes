package com.mobileprism.fishing.model.entity.weather

data class Forecast(
    val forecastday: List<Forecastday> = listOf()
)