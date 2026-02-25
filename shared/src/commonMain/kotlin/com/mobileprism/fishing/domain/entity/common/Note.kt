package com.mobileprism.fishing.domain.entity.common

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateCreated: Long = 0
)
