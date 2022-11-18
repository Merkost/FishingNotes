package com.mobileprism.fishing.model.entity.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class UserData(
    val login: String = "",
    val firstName: String? = "",
    val secondName: String? = "",
    val email: String = "",
    val dateTimeRegistered: String = "",
    val phoneNumber: String? = ""
) : Parcelable