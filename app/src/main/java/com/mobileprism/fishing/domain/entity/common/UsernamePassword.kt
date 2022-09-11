package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsernamePassword(
    val username: String,
    val password: String
) : Parcelable
