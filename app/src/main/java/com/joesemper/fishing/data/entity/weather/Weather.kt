package com.joesemper.fishing.data.entity.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Weather(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("main") val main: String,
    @field:SerializedName("description") val description: String,
    @field:SerializedName("icon") val icon: String
) : Parcelable