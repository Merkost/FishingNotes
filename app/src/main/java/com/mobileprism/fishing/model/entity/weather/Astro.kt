package com.mobileprism.fishing.model.entity.weather

data class Astro(
    val moon_illumination: String = "",
    val moon_phase: String = "",
    val moonrise: String = "",
    val moonset: String = "",
    val sunrise: String = "",
    val sunset: String = ""
)