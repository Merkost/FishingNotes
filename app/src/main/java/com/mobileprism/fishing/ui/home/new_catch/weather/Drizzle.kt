package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Drizzle(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "09"
) : StringOperation, WeatherIconPrefix {
    LightIntensityDrizzle(R.string.light_intensity_drizzle),
    JustDrizzle(R.string.drizzle),
    HeavyIntensityDrizzle(R.string.heavy_intensity_drizzle),
    LightIntensityDrizzleRain(R.string.light_intensity_drizzle_rain),
    DrizzleRain(R.string.drizzle_rain),
    HeavyIntensityDrizzleRain(R.string.heavy_intensity_drizzle_rain),
    ShowerRainAndDrizzle(R.string.shower_rain_and_drizzle),
    HeavyShowerRainAndDrizzle(R.string.heavy_shower_rain_and_drizzle),
    ShowerDrizzle(R.string.shower_drizzle);

    override val getNameRes: Int = R.string.drizzle
}