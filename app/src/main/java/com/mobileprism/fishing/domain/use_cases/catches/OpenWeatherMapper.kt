package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.model.entity.FishingWeather

object OpenWeatherMapper {
    fun getFishingWeather(iconName: String): FishingWeather {
        with(iconName) {
            return when (true) {
                startsWith("01", true) -> {
                    FishingWeather.SUN
                }
                startsWith("02", true) -> {
                    FishingWeather.SUN_AND_CLOUDS
                }
                startsWith("03", true) -> {
                    FishingWeather.CLOUDS
                }
                startsWith("04", true) -> {
                    FishingWeather.BROKEN_CLOUDS
                }
                startsWith("09", true) -> {
                    FishingWeather.HEAVY_RAIN
                }
                startsWith("10", true) -> {
                    FishingWeather.LIGHT_RAIN
                }
                startsWith("11", true) -> {
                    FishingWeather.LIGHTNING
                }
                startsWith("13", true) -> {
                    FishingWeather.SNOW
                }
                startsWith("50", true) -> {
                    FishingWeather.MIST
                }
                else -> {
                    FishingWeather.SUN
                }
            }
        }

    }
}
