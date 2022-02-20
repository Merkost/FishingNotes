package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Flow<RetrofitWrapper<WeatherForecast>>
    suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long): Flow<RetrofitWrapper<WeatherForecast>>
    suspend fun getWeatherForecast(lat: Double, lon: Double): WeatherForecast

}