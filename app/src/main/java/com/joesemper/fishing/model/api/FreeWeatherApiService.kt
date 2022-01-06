package com.joesemper.fishing.model.api

import com.joesemper.fishing.model.entity.weather.CurrentWeatherFree
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FreeWeatherApiService {

    @GET("v1/weather")

    suspend fun getFreeWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
    ): CurrentWeatherFree
}