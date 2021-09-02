package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(lat: Double, lon: Double): Flow<WeatherForecast>
    fun getAllUserMarkersList(): Flow<List<MapMarker>>
}