package com.mobileprism.fishing.domain.entity.content

import androidx.compose.runtime.Immutable
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class UserCatch(
    val id: String = "",
    val userId: String = "",
    val description: String = "",
    val note: Note = Note(),
    val date: Long = 0,
    val fishType: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val fishingRodType: String = "",
    val fishingBait: String = "",
    val fishingLure: String = "",
    val userMarkerId: String = "",
    val placeTitle: String = "",
    val isPublic: Boolean = false,
    val downloadPhotoLinks: List<String> = listOf(),
    val weatherPrimary: String = "",
    val weatherIcon: String = "01",
    val weatherTemperature: Float = 0.0f,
    val weatherWindSpeed: Float = 0.0f,
    val weatherWindDeg: Int = 0,
    val weatherPressure: Int = 0,
    val weatherMoonPhase: Float = 0.0f,
    val lastModified: Long = 0
): Content
