package com.joesemper.fishing.model.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Weather(
    @field:SerializedName("id") val id: Int = 0,
    @field:SerializedName("main") val main: String = "",
    @field:SerializedName("description") val description: String = "Rainy",
    @field:SerializedName("icon") val icon: String = ""
) : Parcelable