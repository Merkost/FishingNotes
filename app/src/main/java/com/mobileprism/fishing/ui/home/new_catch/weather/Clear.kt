package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Clear(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "01"
) : StringOperation, WeatherIconPrefix {
    ClearSky(R.string.clear_sky),
}