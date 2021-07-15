package com.joesemper.fishing.presentation.weather

import com.joesemper.fishing.model.weather.WeatherForecast

sealed class WeatherViewState {
    data class Success(val data: WeatherForecast?) : WeatherViewState()
    data class Error(val error: Throwable) : WeatherViewState()
    data class Loading(val progress: Int?) : WeatherViewState()
}