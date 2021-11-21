package com.joesemper.fishing.compose.ui.home.weather

import android.content.Context
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserMapMarker

fun createCurrentPlaceItem(latLng: LatLng, context: Context): UserMapMarker {
    return UserMapMarker(
        title = context.getString(R.string.current_location),
        latitude = latLng.latitude,
        longitude = latLng.longitude
    )
}