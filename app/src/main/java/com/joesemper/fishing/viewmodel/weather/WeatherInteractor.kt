package com.joesemper.fishing.viewmodel.weather

import com.joesemper.fishing.model.weather.datasource.WeatherDataSource
import com.joesemper.fishing.model.weather.entity.WeatherForecast
import com.joesemper.fishing.model.weather.entity.WeatherState

class WeatherInteractor(private val weatherDataSource: WeatherDataSource<WeatherForecast>) :
        IWeatherInteractor<WeatherState> {

    override suspend fun getData(lat: Float, lon: Float) =
            WeatherState.Success(weatherDataSource.getData(lat = lat, lon = lon))

}