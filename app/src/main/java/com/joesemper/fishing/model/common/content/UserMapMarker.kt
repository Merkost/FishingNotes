package com.joesemper.fishing.model.common.content

import android.os.Parcelable
import com.joesemper.fishing.model.common.content.Content
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserMapMarker(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "My marker",
    val description: String? = "",
    val isPublic: Boolean = false
) : Parcelable, MapMarker