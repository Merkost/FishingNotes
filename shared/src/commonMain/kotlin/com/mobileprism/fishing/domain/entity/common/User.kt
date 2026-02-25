package com.mobileprism.fishing.domain.entity.common

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "Anonymous",
    val photoUrl: String = "",
    val login: String = "",
    val registerDate: Long = 0,
    val birthDate: Long = 0,
)
