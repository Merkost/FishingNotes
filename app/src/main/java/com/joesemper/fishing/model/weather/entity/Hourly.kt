package com.joesemper.fishing.model.weather.entity

import com.google.gson.annotations.SerializedName

class Hourly(
        @field:SerializedName("dt") val date: Int,
        @field:SerializedName("temp") val temperature: Float,
        @field:SerializedName("pressure") val pressure: Int,
        @field:SerializedName("humidity") val humidity: Int,
        @field:SerializedName("clouds") val clouds: Int,
        @field:SerializedName("wind_speed") val windSpeed: Float,
        @field:SerializedName("wind_deg") val windDeg: Int,
        @field:SerializedName("weather") val weather: List<Weather>,
        @field:SerializedName("pop") val probabilityOfPrecipitation: Float,
 )