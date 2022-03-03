package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import kotlinx.coroutines.flow.Flow

interface WeatherPreferences {
    fun getPressureUnitFlow(): Flow<PressureValues>
    fun getTemperatureUnitFlow(): Flow<TemperatureValues>
    fun getWindSpeedUnitFlow(): Flow<WindSpeedValues>
    suspend fun getPressureUnit(): PressureValues
    suspend fun getTemperatureUnit(): TemperatureValues
    suspend fun getWindSpeedUnit(): WindSpeedValues
    suspend fun savePressureUnit(pressureValues: PressureValues)
    suspend fun saveTemperatureUnit(temperatureValues: TemperatureValues)
    suspend fun saveWindSpeedUnit(windSpeedValues: WindSpeedValues)
}