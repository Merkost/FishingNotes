package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import retrofit2.http.GET
import retrofit2.http.Query

interface FreeWeatherApiService {

    @GET("v1/weather")

    suspend fun getFreeWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
    ): CurrentWeatherFree
}