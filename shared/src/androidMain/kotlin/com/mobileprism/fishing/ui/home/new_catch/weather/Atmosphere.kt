package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Atmosphere(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "50",
) : StringOperation, WeatherIconPrefix {
    Mist(R.string.mist),
    Smoke(R.string.smoke),
    Haze(R.string.haze),
    SandOrDustWhirls(R.string.sand_or_dust_whirls),
    Fog(R.string.fog),
    Sand(R.string.sand),
    Dust(R.string.dust),
    VolcanicAsh(R.string.volcanic_ash),
    Squalls(R.string.squalls),
    Tornado(R.string.tornado);

    override val getNameRes: Int = R.string.atmosphere

}