package com.joesemper.fishing.utils

import com.google.android.gms.maps.model.LatLng
import com.joesemper.fishing.model.entity.map.Marker
import java.util.*

fun createMarker(latLng: LatLng) = Marker(
    id = UUID.randomUUID().toString(),
    latitude = latLng.latitude,
    longitude = latLng.longitude
)