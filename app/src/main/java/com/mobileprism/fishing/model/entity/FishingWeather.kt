package com.mobileprism.fishing.model.entity

import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.utils.enums.IconResource
import com.mobileprism.fishing.ui.utils.enums.StringOperation

enum class FishingWeather(override val iconRes: Int, override val stringRes: Int): IconResource, StringOperation {

    SUN(R.drawable.ic_weather_sun, R.string.clear_sky),
    SUN_AND_CLOUDS(R.drawable.ic_weather_cloudly, R.string.broken_clouds),
    CLOUDS(R.drawable.ic_weaether_clouds, R.string.clouds),
    BROKEN_CLOUDS(R.drawable.ic_weather_broken_clouds, R.string.clouds),
    DRIZZLE(R.drawable.ic_weather_light_rain, R.string.drizzle),
    LIGHT_RAIN(R.drawable.ic_weather_light_rain, R.string.light_rain),
    RAIN(R.drawable.ic_weather_hevy_rain, R.string.rain),
    HEAVY_RAIN(R.drawable.ic_weather_hevy_rain, R.string.heavy_intensity_rain),
    LIGHTNING(R.drawable.ic_weather_ligtning, R.string.lightning),
    // TODO: add storm icon STORM(R.drawable.storm, R.string.thunderstorm),
    LIGHT_SNOW(R.drawable.ic_weather_snow, R.string.light_snow),
    SNOW(R.drawable.ic_weather_snow, R.string.snow),
    HEAVY_SNOW(R.drawable.ic_weather_snow, R.string.heavy_snow),
    // TODO: add hail icon HAIL(R.drawable.logout2, R.string.hail),
    // TODO: add sleet icon SLEET(R.drawable.logout2, R.string.sleet),
    MIST(R.drawable.ic_weather_mist, R.string.mist),

}