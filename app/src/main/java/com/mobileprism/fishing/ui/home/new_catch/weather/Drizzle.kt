package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Drizzle(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "09"
) : StringOperation, WeatherIconPrefix {
    LightIntensityDrizzle(R.string.snow),
    JustDrizzle(R.string.snow),
    HeavyIntensityDrizzle(R.string.snow),
    LightIntensityDrizzleRain(R.string.snow),
    DrizzleRain(R.string.snow),
    HeavyIntensityDrizzleRain(R.string.snow),
    ShowerRainAndDrizzle(R.string.snow),
    HeavyShowerRainAndDrizzle(R.string.snow),
    ShowerDrizzle(R.string.snow),
}