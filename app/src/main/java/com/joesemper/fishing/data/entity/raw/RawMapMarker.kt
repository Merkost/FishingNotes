package com.joesemper.fishing.data.entity.raw

data class RawMapMarker (
    val title: String = "My Place",
    val description: String? = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isPublic: Boolean = false
)