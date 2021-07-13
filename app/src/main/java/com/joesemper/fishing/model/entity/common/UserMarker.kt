package com.joesemper.fishing.model.entity.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserMarker(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var title: String = "My marker",
    var description: String? = "",
) : Parcelable