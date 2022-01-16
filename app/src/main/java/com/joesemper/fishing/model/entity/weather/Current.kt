package com.joesemper.fishing.model.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Current(
    @field:SerializedName("dt") val date: Long = 0,
    @field:SerializedName("sunrise") val sunrise: Long = 0,
    @field:SerializedName("sunset") val sunset: Long = 0,
    @field:SerializedName("temp") val temperature: Float = 0.0f,
    @field:SerializedName("pressure") val pressure: Int = 0,
    @field:SerializedName("humidity") val humidity: Int = 0,
    @field:SerializedName("wind_speed") val windSpeed: Float = 0.0f,
    @field:SerializedName("wind_deg") val windDeg: Int = 0,
    @field:SerializedName("weather") val weather: List<Weather> = listOf(Weather())
) : Parcelable