package com.mobileprism.fishing.domain.entity.content

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.model.datasource.room.Converters
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type

@Parcelize
@Entity(tableName = "map_markers")
data class UserMapMarker(
    @PrimaryKey
    var id: String = "",
    var userId: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var title: String = "My marker",
    var description: String = "",
    var markerColor: Int = Color(0xFFEC407A).hashCode(),
    var catchesCount: Int = 0,
    var dateOfCreation: Long = 0,
    var visible: Boolean = true,
    var public: Boolean = false,
    @TypeConverters(Converters::class)
    var notes: List<Note> = listOf(),
) : Parcelable {

    @IgnoredOnParcel
    val latLng: LatLng
        get() = LatLng(latitude, longitude)

}





