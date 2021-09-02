package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.datasource.DatabaseProvider
import com.joesemper.fishing.model.datasource.WeatherProvider

class WeatherRepositoryImpl(
    private val dataProvider: DatabaseProvider,
    private val weatherProvider: WeatherProvider
) : WeatherRepository {

    override fun getAllUserMarkersList() = dataProvider.getAllUserMarkersList()

    override fun getWeather(lat: Double, lon: Double) = weatherProvider.getWeather(lat, lon)
}