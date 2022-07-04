package com.mobileprism.fishing.model.entity.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Token(
    val token: String = ""
) : Parcelable