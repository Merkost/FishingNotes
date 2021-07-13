package com.joesemper.fishing.model.entity.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCatch(
    val id: String = "",
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
    val marker: UserMarker = UserMarker(),
    val isPublic: Boolean = false,
    val photoUris: List<String> = listOf(),
    val downloadPhotoLinks: MutableList<String> = mutableListOf()
): Parcelable
