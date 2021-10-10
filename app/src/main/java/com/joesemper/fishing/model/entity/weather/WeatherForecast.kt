package com.joesemper.fishing.model.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class WeatherForecast(
    @field:SerializedName("lat") val latitude: String,
    @field:SerializedName("lon") val longitude: String,
    @field:SerializedName("timezone_offset") val timezoneOffset: Long,
    @field:SerializedName("hourly") val hourly: List<Hourly>,
    @field:SerializedName("daily") val daily: List<Daily>,
    @field:SerializedName("current") val current: Current
): Parcelable