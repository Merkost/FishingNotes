package com.joesemper.fishing.viewmodels.viewstates

import com.joesemper.fishing.data.entity.weather.WeatherForecast

sealed class WeatherViewState {
    data class Success(val data: WeatherForecast?) : WeatherViewState()
    data class Error(val error: Throwable) : WeatherViewState()
    data class Loading(val progress: Int?) : WeatherViewState()
}