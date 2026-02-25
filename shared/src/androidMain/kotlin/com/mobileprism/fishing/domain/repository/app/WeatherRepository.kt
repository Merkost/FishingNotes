package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Flow<Result<WeatherForecast>>
    suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): Flow<Result<WeatherForecast>>
}