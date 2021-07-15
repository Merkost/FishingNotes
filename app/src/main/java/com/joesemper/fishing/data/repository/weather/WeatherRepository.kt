package com.joesemper.fishing.data.repository.weather

import com.joesemper.fishing.model.weather.WeatherForecast

interface WeatherRepository {
    suspend fun getData(lat: Float, lon: Float): WeatherForecast
}