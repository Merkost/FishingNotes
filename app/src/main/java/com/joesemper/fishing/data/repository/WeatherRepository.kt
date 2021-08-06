package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.content.MapMarker
import com.joesemper.fishing.data.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(lat: Double, lon: Double): Flow<WeatherForecast>
    fun getAllUserMarkersList(): Flow<List<MapMarker>>
}