package com.joesemper.fishing.model.repository.app

import com.joesemper.fishing.model.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(lat: Double, lon: Double): Flow<WeatherForecast>
    suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): Flow<WeatherForecast>
    suspend fun getWeatherForecast(lat: Double, lon: Double): WeatherForecast
}