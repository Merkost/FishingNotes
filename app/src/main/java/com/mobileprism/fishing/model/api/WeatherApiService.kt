package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("onecall")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String? = "metric",
        @Query("exclude") exclude: String? = "minutely,current,alerts",
        @Query("lang") lang: String? = "en",
        @Query("appid") appid: String = "7a364cd23b2ad612f3c716f5eb79b9d2"
    ): WeatherForecast

    @GET("onecall/timemachine")
    suspend fun getHistoricalWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("dt") dt: Long,
        @Query("units") units: String? = "metric",
        @Query("lang") lang: String? = "en",
        @Query("appid") appid: String = "7a364cd23b2ad612f3c716f5eb79b9d2"
    ): WeatherForecast
}

