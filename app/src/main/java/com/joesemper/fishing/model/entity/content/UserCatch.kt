package com.joesemper.fishing.model.entity.content

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCatch(
    val id: String = "",
    val userId: String = "",
    val title: String = "My catch",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val fishType: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val fishingRodType: String = "",
    val fishingBait: String = "",
    val fishingLure: String = "",
    val userMarkerId: String = "",
    @JvmField
    val isPublic: Boolean = false,
    val downloadPhotoLinks: List<String> = listOf(),
): Parcelable, Content
