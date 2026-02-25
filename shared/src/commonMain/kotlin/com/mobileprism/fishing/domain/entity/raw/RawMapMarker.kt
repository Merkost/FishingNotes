package com.mobileprism.fishing.domain.entity.raw

import com.mobileprism.fishing.domain.entity.content.UserMapMarker.Companion.DEFAULT_MARKER_COLOR

data class RawMapMarker (
    val title: String = "My Place",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val markerColor: Int = DEFAULT_MARKER_COLOR,
    val visible: Boolean = true,
    val public: Boolean = false
)
