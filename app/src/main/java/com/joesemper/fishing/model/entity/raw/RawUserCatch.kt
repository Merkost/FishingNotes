package com.joesemper.fishing.model.entity.raw

import java.io.File

data class RawUserCatch(
    val description: String? = "",
    val date: Long = 0,
    val fishType: String = "",
    val fishAmount: Int? = 0,
    val fishWeight: Double? = 0.0,
    val fishingRodType: String? = "",
    val fishingBait: String? = "",
    val fishingLure: String? = "",
    val userMarkerId: String? = "",
    val markerId: String = "",
    val isPublic: Boolean = false,
    val includeWeather: Boolean = true,
    val photos: List<File> = listOf(),
    val weatherPrimary: String = "",
    val weatherIcon: String = "",
    val weatherTemperature: Float = 0.0f,
    val weatherWindSpeed: Float = 0.0f,
    val weatherWindDeg: Int = 0,
    val weatherPressure: Int = 0,
    val weatherMoonPhase: Float = 0.0f
)