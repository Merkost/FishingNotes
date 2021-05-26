package com.joesemper.fishing.model.weather.datasource.api

import com.joesemper.fishing.model.weather.entity.WeatherForecast
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("onecall")
    suspend fun getWeather(
            @Query("lat") latitude: Float,
            @Query("lon") longitude: Float,
            @Query("units") units: String? = "metric",
            @Query("exclude") exclude: String? = "minutely,current,alerts",
            @Query("lang") lang: String? = "ru",
            @Query("appid") appid: String = "33da5092c9df576f30d4bfe2788922a4"
    ): WeatherForecast

}