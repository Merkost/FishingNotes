package com.joesemper.fishing.model.entity.raw

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
    val markerId: String = "",
    val isPublic: Boolean = false,
    val includeWeather: Boolean = true,
    val photos: List<ByteArray> = listOf(),
)