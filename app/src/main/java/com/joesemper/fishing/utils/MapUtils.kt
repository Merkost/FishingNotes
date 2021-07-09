package com.joesemper.fishing.utils

import com.google.android.gms.maps.model.LatLng
import com.joesemper.fishing.model.entity.map.UserMarker

fun createUserMarker(latLng: LatLng, title: String,  description: String?, photoUri: String?) = UserMarker(
    id = getRandomString(10),
    latitude = latLng.latitude,
    longitude = latLng.longitude,
    title = title,
    description = description,
    photoUri = photoUri ?: ""
)