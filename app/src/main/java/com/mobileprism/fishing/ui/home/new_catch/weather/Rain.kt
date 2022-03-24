package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Rain(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "10"
) : StringOperation, WeatherIconPrefix {
    LightRain(R.string.light_rain),
    ModerateRain(R.string.rain),
    HeavyIntensityRain(R.string.heavy_intensity_rain),
    VeryHeavyRain(R.string.very_heavy_rain),
    ExtremeRain(R.string.extreme_rain),
    FreezingRain(R.string.freezing_rain, "13"),
    LightIntensityShowerRain(R.string.light_intensity_shower_rain, "09"),
    ShowerRain(R.string.shower_rain, "09"),
    HeavyIntensityShowerRain(R.string.heavy_intensity_shower_rain, "09"),
    RaggedShowerRain(R.string.ragged_shower_rain, "09");

    override val getNameRes: Int = R.string.rain
}