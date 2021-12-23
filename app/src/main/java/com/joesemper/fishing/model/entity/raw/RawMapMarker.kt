package com.joesemper.fishing.model.entity.raw

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color

data class RawMapMarker (
    val title: String = "My Place",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val markerColor: Int = Color(0xFFEC407A).hashCode(),
    val visible: Boolean = true,
    val public: Boolean = false
)