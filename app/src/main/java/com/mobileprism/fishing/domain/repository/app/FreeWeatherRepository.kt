package com.mobileprism.fishing.domain.repository.app


import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import kotlinx.coroutines.flow.Flow

interface FreeWeatherRepository {
    suspend fun getCurrentWeatherFree(lat: Double, lon: Double): Flow<Result<CurrentWeatherFree>>

}