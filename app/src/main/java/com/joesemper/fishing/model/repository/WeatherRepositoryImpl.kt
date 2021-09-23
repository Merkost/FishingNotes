package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.datasource.DatabaseProvider
import com.joesemper.fishing.model.datasource.WeatherProvider
import com.joesemper.fishing.model.entity.weather.WeatherForecast

class WeatherRepositoryImpl(
    private val dataProvider: DatabaseProvider,
    private val weatherProvider: WeatherProvider
) : WeatherRepository {

    override fun getAllUserMarkersList() = dataProvider.getAllUserMarkersList()

    override fun getWeather(lat: Double, lon: Double) = weatherProvider.getWeather(lat, lon)

    override suspend fun getHistoricalWeather(
        lat: Double,
        lon: Double,
        date: Long
    ): WeatherForecast = weatherProvider.getHistoricalWeather(lat, lon, date)

    override suspend fun getWeatherForecast(lat: Double, lon: Double): WeatherForecast =
        weatherProvider.getWeatherForecast(lat, lon)
}