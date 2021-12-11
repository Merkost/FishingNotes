package com.joesemper.fishing.model.entity.raw

data class NewCatchWeather(
    val weatherDescription: String,
    val icon: String,
    val temperatureInC: Int,
    val pressureInMmhg: Float,
    val windInMs: Int,
    val moonPhase: Int
    )
