package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Snow(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "13"
) : StringOperation, WeatherIconPrefix {
    LightSnow(R.string.light_snow),
    JustSnow(R.string.snow),
    HeavySnow(R.string.heavy_snow),
    Sleet(R.string.sleet),
    LightShowerSleet(R.string.light_shower_sleet),
    ShowerSleet(R.string.shower_sleet),
    LightRainAndSnow(R.string.light_rain_and_snow),
    RainAndSnow(R.string.rain_and_snow),
    LightShowerSnow(R.string.light_shower_snow),
    ShowerSnow(R.string.shower_snow),
    HeavyShowerSnow(R.string.heavy_shower_snow);

    override val getNameRes: Int = R.string.snow
}