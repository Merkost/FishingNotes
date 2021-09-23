package com.joesemper.fishing.model.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Current(
    @field:SerializedName("dt") val date: Long,
    @field:SerializedName("sunrise") val sunrise: Long,
    @field:SerializedName("sunset") val sunset: Long,
    @field:SerializedName("temp") val temperature: Float,
    @field:SerializedName("pressure") val pressure: Int,
    @field:SerializedName("humidity") val humidity: Int,
    @field:SerializedName("wind_speed") val windSpeed: Float,
    @field:SerializedName("wind_deg") val windDeg: Int,
    @field:SerializedName("weather") val weather: List<Weather>
) : Parcelable