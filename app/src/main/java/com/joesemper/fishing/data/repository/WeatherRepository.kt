package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.weather.WeatherForecast

interface WeatherRepository {
    suspend fun getData(lat: Float, lon: Float): WeatherForecast
}