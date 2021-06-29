package com.joesemper.fishing.model.entity.map

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserMarker(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "My marker",
    val description: String? = ""
): Parcelable
