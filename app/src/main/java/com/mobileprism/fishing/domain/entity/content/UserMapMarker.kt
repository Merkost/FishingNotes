package com.mobileprism.fishing.domain.entity.content

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.parcelize.Parcelize

@Parcelize
class UserMapMarker(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var title: String = "My marker",
    var description: String = "",
    var markerColor: Int = Color(0xFFEC407A).hashCode(),
    val catchesCount: Int = 0,
    val dateOfCreation: Long = 0,
    val visible: Boolean = true,
    val public: Boolean = false,
    var notes: List<Note> = listOf()
) : Parcelable, MapMarker {

    val latLng: LatLng
    get() = LatLng(latitude, longitude)

}