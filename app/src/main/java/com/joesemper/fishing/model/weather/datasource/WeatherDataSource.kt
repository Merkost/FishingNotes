package com.joesemper.fishing.model.weather.datasource

interface WeatherDataSource<T> {
    suspend fun getData(lat: Float, lon: Float): T
}