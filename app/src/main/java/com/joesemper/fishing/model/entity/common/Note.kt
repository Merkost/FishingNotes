package com.joesemper.fishing.model.entity.common

import android.app.ActivityManager
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateCreated: Long = 0
) : Parcelable
