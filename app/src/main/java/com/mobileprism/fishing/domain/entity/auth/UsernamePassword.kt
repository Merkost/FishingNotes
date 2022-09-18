package com.mobileprism.fishing.domain.entity.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsernamePassword(
    val username: String,
    val password: String
) : Parcelable
