package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmailPassword(
    val email: String,
    val password: String
) : Parcelable