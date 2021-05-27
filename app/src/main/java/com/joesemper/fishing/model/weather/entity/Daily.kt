package com.joesemper.fishing.model.weather.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Daily(
        @field:SerializedName("dt") val date: Long,
        @field:SerializedName("sunrise") val sunrise: Long,
        @field:SerializedName("sunset") val sunset: Long,
        @field:SerializedName("moon_phase") val moonPhase: Float,
        @field:SerializedName("pressure") val pressure: Int,
        @field:SerializedName("humidity") val humidity: Int,
        @field:SerializedName("wind_speed") val windSpeed: Float,
        @field:SerializedName("wind_deg") val windDeg: Int,
        @field:SerializedName("weather") val weather: List<Weather>,
        @field:SerializedName("temp") val temperature: Temperature
) : Parcelable

@Parcelize
class Temperature(
        @field:SerializedName("day") val day: Float,
        @field:SerializedName("min") val min: Float,
        @field:SerializedName("max") val max: Float,
        @field:SerializedName("night") val night: Float,
        @field:SerializedName("eve") val evening: Float,
        @field:SerializedName("morn") val morning: Float,
) : Parcelable