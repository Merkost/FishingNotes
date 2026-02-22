package com.mobileprism.fishing.domain.entity.weather

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class WeatherForecast(
    @SerialName("lat") val latitude: String = "0.0",
    @SerialName("lon") val longitude: String = "0.0",
    @SerialName("timezone_offset") val timezoneOffset: Long = 0,
    @SerialName("hourly") val hourly: List<Hourly> = (1..6).map { Hourly() },
    @SerialName("daily") val daily: List<Daily> = (1..6).map { Daily() },
    @SerialName("current") val current: Current = Current()
): Parcelable
