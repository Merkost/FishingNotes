package com.mobileprism.fishing.domain.entity.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoogleAuthRequest(
    val email: String,
    val googleAuthId: String
) : Parcelable

