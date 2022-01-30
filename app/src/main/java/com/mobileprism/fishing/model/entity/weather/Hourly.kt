package com.mobileprism.fishing.model.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Hourly(
    @field:SerializedName("dt") val date: Long = 0,
    @field:SerializedName("temp") val temperature: Float = 0f,
    @field:SerializedName("pressure") val pressure: Int = 0,
    @field:SerializedName("humidity") val humidity: Int = 0,
    @field:SerializedName("clouds") val clouds: Int = 0,
    @field:SerializedName("wind_speed") val windSpeed: Float = 0f,
    @field:SerializedName("wind_deg") val windDeg: Int = 0,
    @field:SerializedName("weather") val weather: List<Weather> = listOf(Weather()),
    @field:SerializedName("pop") val probabilityOfPrecipitation: Float = 0f,
): Parcelable