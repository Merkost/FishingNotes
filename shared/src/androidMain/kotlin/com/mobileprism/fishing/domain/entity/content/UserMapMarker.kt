package com.mobileprism.fishing.domain.entity.content

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class UserMapMarker(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "My marker",
    val description: String = "",
    val markerColor: Int = Color(0xFFEC407A).hashCode(),
    val catchesCount: Int = 0,
    val dateOfCreation: Long = 0,
    val visible: Boolean = true,
    val public: Boolean = false,
    val notes: List<Note> = listOf(),
    val lastModified: Long = 0
) : Parcelable, MapMarker {

    val latLng: LatLng
    get() = LatLng(latitude, longitude)

}
