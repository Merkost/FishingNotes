package com.joesemper.fishing.model.entity.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
data class User(
    val uid: String = "1",
    val email: String = "",
    val displayName: String = "Anonymous",
    val isAnonymous: Boolean = true,
    val photoUrl: String? = null,
    val login: String = "",
    val registerDate: Long = 0,
): Parcelable