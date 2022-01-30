package com.mobileprism.fishing.model.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherForecast(
    @field:SerializedName("lat") val latitude: String = "0.0",
    @field:SerializedName("lon") val longitude: String = "0.0",
    @field:SerializedName("timezone_offset") val timezoneOffset: Long = 0,
    @field:SerializedName("hourly") val hourly: List<Hourly> = (1..6).map { Hourly() },
    @field:SerializedName("daily") val daily: List<Daily> = (1..6).map { Daily() },
    @field:SerializedName("current") val current: Current = Current()
): Parcelable