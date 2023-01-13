package com.mobileprism.fishing.domain.entity.auth.restore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RestoreRemoteFind(
    val login: String,
) : Parcelable

@Parcelize
data class RestoreRemoteConfirm(
    val login: String,
    val otp: Int,
) : Parcelable

@Parcelize
data class RestoreRemoteReset(
    val login: String,
    val newPassword: String,
) : Parcelable