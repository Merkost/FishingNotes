package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateCreated: Long = 0
) : Parcelable
