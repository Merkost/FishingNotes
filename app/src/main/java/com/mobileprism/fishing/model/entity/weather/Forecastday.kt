package com.mobileprism.fishing.model.entity.weather

data class Forecastday(
    val astro: Astro = Astro(),
    val date: String = "",
    val date_epoch: Int = 0,
    val day: Day = Day(),
    val hour: List<Hour> = listOf()
)