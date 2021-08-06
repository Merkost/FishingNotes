package com.joesemper.fishing.data.datasource

import com.joesemper.fishing.data.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherProvider {
    fun getWeather(lat: Double, lon: Double): Flow<WeatherForecast>
}