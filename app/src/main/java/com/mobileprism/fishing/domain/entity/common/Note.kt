package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateCreated: Long = 0
) : Parcelable
