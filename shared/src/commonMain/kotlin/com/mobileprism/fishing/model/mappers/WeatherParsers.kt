package com.mobileprism.fishing.model.mappers

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

fun getWeatherIconByName(name: String): DrawableResource {
    return when (true) {
        name.startsWith("01", true) -> {
            Res.drawable.ic_weather_sun
        }
        name.startsWith("02", true) -> {
            Res.drawable.ic_weather_cloudly
        }
        name.startsWith("03", true) -> {
            Res.drawable.ic_weaether_clouds
        }
        name.startsWith("04", true) -> {
            Res.drawable.ic_weather_broken_clouds
        }
        name.startsWith("09", true) -> {
            Res.drawable.ic_weather_hevy_rain
        }
        name.startsWith("10", true) -> {
            Res.drawable.ic_weather_light_rain
        }
        name.startsWith("11", true) -> {
            Res.drawable.ic_weather_ligtning
        }
        name.startsWith("13", true) -> {
            Res.drawable.ic_weather_snow
        }
        name.startsWith("50", true) -> {
            Res.drawable.ic_weather_mist
        }
        else -> {
            Res.drawable.ic_weather_sun
        }
    }
}

fun getWeatherNameByIcon(res: DrawableResource): String {
    return when (res) {
        Res.drawable.ic_weather_sun -> "01"
        Res.drawable.ic_weather_cloudly -> "02"
        Res.drawable.ic_weaether_clouds -> "03"
        Res.drawable.ic_weather_broken_clouds -> "04"
        Res.drawable.ic_weather_hevy_rain -> "09"
        Res.drawable.ic_weather_light_rain -> "10"
        Res.drawable.ic_weather_ligtning -> "11"
        Res.drawable.ic_weather_snow -> "13"
        Res.drawable.ic_weather_mist -> "50"
        else -> "01"
    }
}

fun getAllWeatherIcons() =
    listOf(
        Res.drawable.ic_weather_sun,
        Res.drawable.ic_weather_cloudly,
        Res.drawable.ic_weaether_clouds,
        Res.drawable.ic_weather_broken_clouds,
        Res.drawable.ic_weather_hevy_rain,
        Res.drawable.ic_weather_light_rain,
        Res.drawable.ic_weather_ligtning,
        Res.drawable.ic_weather_snow,
        Res.drawable.ic_weather_mist,
    )

fun getMoonIconByPhase(phase: Float): DrawableResource {
    return when {
        phase <= 0.02f -> {
            Res.drawable.moon_new
        }
        phase <= 0.13f -> {
            Res.drawable.moon_waxing_crescent
        }
        phase <= 0.25f -> {
            Res.drawable.moon_first_quarter
        }
        phase <= 0.45f -> {
            Res.drawable.moon_waxing_gibbous
        }
        phase <= 0.55f -> {
            Res.drawable.moon_full
        }
        phase <= 0.75f -> {
            Res.drawable.moon_waning_gibbous
        }
        phase <= 0.87f -> {
            Res.drawable.moon_last_quarter
        }
        phase <= 0.98f -> {
            Res.drawable.moon_waning_crescent
        }
        phase <= 1.0f -> {
            Res.drawable.moon_new
        }
        else -> {
            Res.drawable.moon_new
        }
    }
}
