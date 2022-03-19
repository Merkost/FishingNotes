package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Clouds(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String
) : StringOperation, WeatherIconPrefix {
    FewClouds(R.string.few_clouds, "02"), //11-25%
    ScatteredClouds(R.string.scattered_clouds, "03"), //25-50%
    BrokenClouds(R.string.broken_clouds, "04"), //51-84%
    OvercastClouds(R.string.overcast_clouds, "04"); //85-100%

    override val getNameRes: Int = R.string.overcast_clouds
}