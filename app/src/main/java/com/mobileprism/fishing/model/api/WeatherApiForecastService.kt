package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.model.entity.weather.WeatherApiForecast
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiForecastService {

    @GET("forecast.json")
    suspend fun getWeather(
        @Query("key") apiKey: String = "941227bc9d6d4876bbd100651220706",
        @Query("q") coordinates: String,
        @Query("days") days: Int = 3,
        @Query("aqi") airQuality: String = "no",
        @Query("alerts") alerts: String = "no",
    ): WeatherApiForecast
}