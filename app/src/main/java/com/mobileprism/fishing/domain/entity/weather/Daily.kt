package com.mobileprism.fishing.domain.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Daily(
    @field:SerializedName("dt") val date: Long = 0,
    @field:SerializedName("sunrise") val sunrise: Long = 0,
    @field:SerializedName("sunset") val sunset: Long = 0,
    @field:SerializedName("moon_phase") val moonPhase: Float = 0f,
    @field:SerializedName("pressure") val pressure: Int = 0,
    @field:SerializedName("humidity") val humidity: Int = 0,
    @field:SerializedName("wind_speed") val windSpeed: Float = 0f,
    @field:SerializedName("wind_deg") val windDeg: Int = 0,
    @field:SerializedName("weather") val weather: List<Weather> = listOf(Weather()),
    @field:SerializedName("temp") val temperature: Temperature = Temperature(),
    @field:SerializedName("pop") val probabilityOfPrecipitation: Float = 0f,
    @field:SerializedName("clouds") val clouds: Int = 0,
) : Parcelable

@Parcelize
class Temperature(
    @field:SerializedName("day") val day: Float = 0f,
    @field:SerializedName("min") val min: Float = 0f,
    @field:SerializedName("max") val max: Float = 0f,
    @field:SerializedName("night") val night: Float = 0f,
    @field:SerializedName("eve") val evening: Float = 0f,
    @field:SerializedName("morn") val morning: Float = 0f,
) : Parcelable