package com.joesemper.fishing.model.entity.common

import android.app.ActivityManager
import java.util.*

data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateCreated: Long = Calendar.getInstance().timeInMillis
)
