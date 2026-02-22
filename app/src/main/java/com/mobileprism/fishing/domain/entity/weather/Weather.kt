package com.mobileprism.fishing.domain.entity.weather

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class Weather(
    @SerialName("id") val id: Int = 0,
    @SerialName("main") val main: String = "",
    @SerialName("description") val description: String = "Rainy",
    @SerialName("icon") val icon: String = ""
) : Parcelable
