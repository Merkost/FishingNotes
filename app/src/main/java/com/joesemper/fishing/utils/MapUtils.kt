package com.joesemper.fishing.utils

import com.google.android.gms.maps.model.LatLng
import com.joesemper.fishing.model.entity.map.UserMarker
import java.util.*

fun createUserMarker(latLng: LatLng, title: String,  description: String?) = UserMarker(
    id = UUID.randomUUID().toString(),
    latitude = latLng.latitude,
    longitude = latLng.longitude,
    title = title,
    description = description
)