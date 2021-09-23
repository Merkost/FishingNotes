package com.joesemper.fishing.model.datasource

import com.joesemper.fishing.model.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherProvider {
    fun getWeather(lat: Double, lon: Double): Flow<WeatherForecast>
    suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): WeatherForecast
}