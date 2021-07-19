package com.joesemper.fishing.data.entity

import com.joesemper.fishing.model.common.content.MapMarker

data class RawUserCatch (
    val title: String = "My marker",
    val description: String? = "",
    val date: String = "",
    val time: String = "",
    val fishType: String = "",
    val fishAmount: Int? = 0,
    val fishWeight: Double? = 0.0,
    val fishingRodType: String? = "",
    val fishingBait: String? = "",
    val fishingLure: String? = "",
    val userMarkerId: String? = "",
    val marker: MapMarker = MapMarker(),
    val isPublic: Boolean = false,
    val includeWeather: Boolean = true,
    val photos: List<ByteArray> = listOf(),
)