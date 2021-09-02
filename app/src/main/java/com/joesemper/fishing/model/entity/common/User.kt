package com.joesemper.fishing.model.entity.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class User(
    val userId: String = "1",
    val userName: String = "Anonymous",
    val isAnonymous: Boolean = true,
    val userPic: String? = null,
): Parcelable