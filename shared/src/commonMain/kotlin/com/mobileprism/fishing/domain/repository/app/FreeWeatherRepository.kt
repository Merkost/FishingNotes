package com.mobileprism.fishing.domain.repository.app


import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree

interface FreeWeatherRepository {
    suspend fun getCurrentWeatherFree(lat: Double, lon: Double): Result<CurrentWeatherFree>
}