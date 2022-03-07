package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Snow(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "13"
) : StringOperation, WeatherIconPrefix {
    LightSnow(R.string.snow),
    JustSnow(R.string.snow),
    HeavySnow(R.string.snow),
    Sleet(R.string.snow),
    LightShowerSleet(R.string.snow),
    ShowerSleet(R.string.snow),
    LightRainAndSnow(R.string.snow),
    RainAndSnow(R.string.snow),
    LightShowerSnow(R.string.snow),
    ShowerSnow(R.string.snow),
    HeavyShowerSnow(R.string.snow);

    override val getNameRes: Int = R.string.snow
}