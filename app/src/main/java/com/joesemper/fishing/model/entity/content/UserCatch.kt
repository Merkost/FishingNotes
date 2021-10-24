package com.joesemper.fishing.model.entity.content

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCatch(
    val id: String = "",
    val userId: String = "",
    val description: String = "",
    val date: Long = 0,
    val fishType: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val fishingRodType: String = "",
    val fishingBait: String = "",
    val fishingLure: String = "",
    val userMarkerId: String = "",
    val placeTitle: String = "",
    @JvmField
    val isPublic: Boolean = false,
    val downloadPhotoLinks: List<String> = listOf(),
    val weatherPrimary: String = "",
    val weatherIcon: String = "",
    val weatherTemperature: Float = 0.0f,
    val weatherWindSpeed: Float = 0.0f,
    val weatherWindDeg: Int = 0,
    val weatherPressure: Int = 0,
    val weatherMoonPhase: Float = 0.0f
): Parcelable, Content
