package com.joesemper.fishing.model.repository.app

import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.weather.CurrentWeatherFree
import kotlinx.coroutines.flow.Flow

interface FreeWeatherRepository {
    suspend fun getCurrentWeatherFree(lat: Double, lon: Double): Flow<RetrofitWrapper<CurrentWeatherFree>>

}