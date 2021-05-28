package com.joesemper.fishing.model.weather.datasource

import com.joesemper.fishing.model.weather.entity.WeatherForecast

interface WeatherRepository {
    suspend fun getData(lat: Float, lon: Float): WeatherForecast
}