package com.joesemper.fishing.viewmodel.weather

interface IWeatherInteractor<T> {
    suspend fun getData(lat: Float, lon: Float): T
}