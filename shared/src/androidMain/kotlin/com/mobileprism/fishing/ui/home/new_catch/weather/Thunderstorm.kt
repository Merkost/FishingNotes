package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.annotation.StringRes
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class Thunderstorm(
    @StringRes
    override val stringRes: Int,
    override val iconPrefix: String = "11"
) : StringOperation, WeatherIconPrefix {
    ThunderstormWithLightRain(R.string.thunderstorm_with_light_rain),
    ThunderstormWithRain(R.string.thunderstorm_with_rain),
    ThunderstormWithHeavyRain(R.string.thunderstorm_with_heavy_rain),
    LightThunderstorm(R.string.light_thunderstorm),
    JustThunderstorm(R.string.thunderstorm),
    HeavyThunderstorm(R.string.heavy_thunderstorm),
    RaggedThunderstorm(R.string.ragged_thunderstorm),
    ThunderstormWithLightDrizzle(R.string.thunderstorm_with_light_drizzle),
    ThunderstormWithDrizzle(R.string.thunderstorm_with_drizzle),
    ThunderstormWithHeavyDrizzle(R.string.thunderstorm_with_heavy_drizzle);

    override val getNameRes: Int = R.string.thunderstorm
}