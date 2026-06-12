package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.entity.weather.WeatherResult
import com.mobileprism.fishing.domain.entity.weather.WeatherSource

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherForecast>
    suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): Result<WeatherForecast>

    suspend fun getWeatherWithMeta(lat: Double, lon: Double): Result<WeatherResult> {
        return getWeather(lat, lon).map { forecast ->
            WeatherResult(forecast = forecast, source = WeatherSource.FRESH)
        }
    }
}