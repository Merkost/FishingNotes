package com.mobileprism.fishing.model.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val uid: String = "1",
    val email: String = "",
    val displayName: String = "Anonymous",
    val photoUrl: String = "",
    val login: String = "",
    val registerDate: Long = 0,
): Parcelable