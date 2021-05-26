package com.joesemper.fishing.model.weather.entity

sealed class WeatherState {
    data class Success(val data: WeatherForecast?) : WeatherState()
    data class Error(val error: Throwable) : WeatherState()
    data class Loading(val progress: Int?) : WeatherState()
}