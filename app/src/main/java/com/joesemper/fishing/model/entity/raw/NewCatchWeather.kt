package com.joesemper.fishing.model.entity.raw

data class NewCatchWeather(
    var weatherDescription: String = "",
    var icon: String = "",
    var temperatureInC: Int = 0,
    var pressureInMmhg: Int = 0,
    var windInMs: Int = 0,
    var moonPhase: Float = 0f
    )
