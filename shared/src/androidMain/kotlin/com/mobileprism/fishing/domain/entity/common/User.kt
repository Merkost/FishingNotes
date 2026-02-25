package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "Anonymous",
    val photoUrl: String = "",
    val login: String = "",
    val registerDate: Long = 0,
    val birthDate: Long = 0,
): Parcelable
