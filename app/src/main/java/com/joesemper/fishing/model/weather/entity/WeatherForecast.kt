package com.joesemper.fishing.model.weather.entity

import com.google.gson.annotations.SerializedName

class WeatherForecast(
    @field:SerializedName("lat") val latitude: String,
    @field:SerializedName("lon") val longitude: String,
    @field:SerializedName("hourly") val hourly: List<Hourly>,
    @field:SerializedName("daily") val daily: List<Daily>
)