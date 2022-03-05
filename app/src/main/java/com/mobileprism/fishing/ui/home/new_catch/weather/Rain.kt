package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Rain(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "10"
) : StringOperation, WeatherIconPrefix {
    LightRain(R.string.snow),
    ModerateRain(R.string.snow),
    HeavyIntensityRain(R.string.snow),
    VeryHeavyRain(R.string.snow),
    ExtremeRain(R.string.snow),
    FreezingRain(R.string.snow, "13"),
    LightIntensityShowerRain(R.string.snow, "09"),
    ShowerRain(R.string.snow, "09"),
    HeavyIntensityShowerRain(R.string.snow, "09"),
    RaggedShowerRain(R.string.snow, "09"),
}