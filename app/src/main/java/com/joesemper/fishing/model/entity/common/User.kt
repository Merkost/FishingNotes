package com.joesemper.fishing.model.entity.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val userId: String,
    val userName: String? = "Anonymous",
    val isAnonymous: Boolean,
    val userPic: String?,
): Parcelable