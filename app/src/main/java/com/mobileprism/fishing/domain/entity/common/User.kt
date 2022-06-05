package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Parcelize
data class User(
    val uid: String = "0",
    val email: String = "",
    val displayName: String = "User",
    val photoUrl: String = "",
    val login: String = "",
    val registerDate: Long = Calendar.getInstance().timeInMillis,
    val birthDate: Long = 0,
    val loginType: LoginType? = null,
): Parcelable

enum class LoginType {
    LOCAL,
    GOOGLE;
}
