package com.joesemper.fishing.domain.viewstates

import com.joesemper.fishing.model.entity.weather.WeatherForecast

sealed class WeatherViewState {
    data class Success(val data: WeatherForecast?) : WeatherViewState()
    data class Error(val error: Throwable) : WeatherViewState()
    data class Loading(val progress: Int? = 0) : WeatherViewState()
}