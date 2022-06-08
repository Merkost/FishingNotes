package com.mobileprism.fishing.model.entity.weather

data class Day(
    val avghumidity: Double = 0.0,
    val avgtemp_c: Double = 0.0,
    val avgtemp_f: Double = 0.0,
    val avgvis_km: Double = 0.0,
    val avgvis_miles: Double = 0.0,
    val condition: Condition = Condition(),
    val daily_chance_of_rain: Int = 0,
    val daily_chance_of_snow: Int = 0,
    val daily_will_it_rain: Int = 0,
    val daily_will_it_snow: Int = 0,
    val maxtemp_c: Double = 0.0,
    val maxtemp_f: Double = 0.0,
    val maxwind_kph: Double = 0.0,
    val maxwind_mph: Double = 0.0,
    val mintemp_c: Double = 0.0,
    val mintemp_f: Double = 0.0,
    val totalprecip_in: Double = 0.0,
    val totalprecip_mm: Double = 0.0,
    val uv: Double = 0.0
)