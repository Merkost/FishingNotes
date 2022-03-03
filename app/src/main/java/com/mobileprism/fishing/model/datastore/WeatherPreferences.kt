package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import kotlinx.coroutines.flow.Flow

interface WeatherPreferences {
    fun getPressureUnit(): Flow<PressureValues>
    fun getTemperatureUnit(): Flow<TemperatureValues>
    fun getWindSpeedUnit(): Flow<WindSpeedValues>
    suspend fun savePressureUnit(pressureValues: PressureValues)
    suspend fun saveTemperatureUnit(temperatureValues: TemperatureValues)
    suspend fun saveWindSpeedUnit(windSpeedValues: WindSpeedValues)
}