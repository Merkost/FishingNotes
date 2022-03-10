package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.ui.viewstates.RetrofitWrapper
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Flow<Result<WeatherForecast>>
    suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): Flow<Result<WeatherForecast>>
}