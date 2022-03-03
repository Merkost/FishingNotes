package com.mobileprism.fishing.ui.home.new_catch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewCatchWeatherData(
    var weatherDescription: String = "",
    var icon: String = "",
    var temperature: String = "",
    var pressure: String = "",
    var wind: String = "",
    var windDir: Int = 0,
    var moonPhase: Float = 0f
) : Parcelable
