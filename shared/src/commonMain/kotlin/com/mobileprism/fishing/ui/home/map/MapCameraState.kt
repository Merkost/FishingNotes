package com.mobileprism.fishing.ui.home.map

data class MapCameraState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val zoom: Float = 0f,
    val bearing: Float = 0f,
)
