package com.mobileprism.fishing.ui.home.weather

import android.os.Parcelable
import com.mobileprism.fishing.domain.entity.weather.Daily
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DailyWeatherData(
    val selectedDay: Int,
    val dailyForecast: List<Daily>
) : Parcelable