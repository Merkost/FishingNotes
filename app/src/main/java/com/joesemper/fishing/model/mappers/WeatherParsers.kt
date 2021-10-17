package com.joesemper.fishing.model.mappers

import com.joesemper.fishing.R

fun getWeatherIconByName(name: String): Int {
    return when (true) {
        name.startsWith("01", true) -> {
            R.drawable.weather_sunny
        }
        name.startsWith("02", true) -> {
            R.drawable.weather_partly_cloudy
        }
        name.startsWith("03", true) -> {
            R.drawable.weather_cloudy
        }
        name.startsWith("04", true) -> {
            R.drawable.weather_cloudy
        }
        name.startsWith("09", true) -> {
            R.drawable.weather_pouring
        }
        name.startsWith("10", true) -> {
            R.drawable.weather_partly_rainy
        }
        name.startsWith("11", true) -> {
            R.drawable.weather_lightning
        }
        name.startsWith("13", true) -> {
            R.drawable.weather_snowy
        }
        name.startsWith("50", true) -> {
            R.drawable.weather_hazy
        }
        else -> {
            R.drawable.weather_sunny
        }
    }
}

fun getMoonIconByPhase(phase: Float): Int {
    return when {
        phase <= 0.02f -> {
            R.drawable.moon_new
        }
        phase <= 0.13f -> {
            R.drawable.moon_waxing_crescent
        }
        phase <= 0.25f -> {
            R.drawable.moon_first_quarter
        }
        phase <= 0.45f -> {
            R.drawable.moon_waxing_gibbous
        }
        phase <= 0.55f -> {
            R.drawable.moon_full
        }
        phase <= 0.75f -> {
            R.drawable.moon_waning_gibbous
        }
        phase <= 0.87f -> {
            R.drawable.moon_last_quarter
        }
        phase <= 0.98f -> {
            R.drawable.moon_waning_crescent
        }
        phase <= 0.1f -> {
            R.drawable.moon_new
        }
        else -> {
            R.drawable.moon_full
        }
    }
}