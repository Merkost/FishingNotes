package com.mobileprism.fishing.model.entity.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserResponse(
    val token: String,
    val user: UserData
) : Parcelable