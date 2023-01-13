package com.mobileprism.fishing.domain.entity.content.firebase

import android.os.Parcelable
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.parcelize.Parcelize

@Parcelize
data class FirebaseUserCatch(
    var id: String = "",
    var markerId: String = "",
    var userId: String = "",
    var description: String = "",
    var note: Note = Note(),
    var date: Long = 0,
    var dateOfCreation: Long = 0,
    var fishType: String = "",
    var fishAmount: Int = 0,
    var fishWeight: Double = 0.0,
    var fishingRodType: String = "",
    var fishingBait: String = "",
    var fishingLure: String = "",
    var placeTitle: String = "",
    var isPublic: Boolean = false,
    var downloadPhotoLinks: List<String> = listOf(),
    var weatherPrimary: String = "",
    var weatherIcon: String = "01",
    var weatherTemperature: Float = 0.0f,
    var weatherWindSpeed: Float = 0.0f,
    var weatherWindDeg: Int = 0,
    var weatherPressure: Int = 0,
    var weatherMoonPhase: Float = 0.0f
) : Parcelable
