package com.joesemper.fishing.model.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCatch(
    val id: String = "",
    var userId: String = "",
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
    var userMarkerId: String? = "",
    var marker: UserMarker? = UserMarker(),
    @JvmField
    val isPublic: Boolean = false,
    val photoUris: List<String> = listOf(),
    val downloadPhotoLinks: MutableList<String> = mutableListOf()
): Parcelable, MapMarker
