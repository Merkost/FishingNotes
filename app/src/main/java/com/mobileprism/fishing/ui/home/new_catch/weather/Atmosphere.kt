package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Atmosphere(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "50",
) : StringOperation, WeatherIconPrefix {
    Mist(R.string.snow),
    Smoke(R.string.snow),
    Haze(R.string.snow),
    SandOrDustWhirls(R.string.snow),
    Fog(R.string.snow),
    Sand(R.string.snow),
    Dust(R.string.snow),
    VolcanicAsh(R.string.snow),
    Squalls(R.string.snow),
    Tornado(R.string.snow);

    override val getNameRes: Int = R.string.atmosphere

}