package com.joesemper.fishing.model.repository.weather

import com.joesemper.fishing.model.entity.weather.WeatherForecast

interface WeatherRepository {
    suspend fun getData(lat: Float, lon: Float): WeatherForecast
}