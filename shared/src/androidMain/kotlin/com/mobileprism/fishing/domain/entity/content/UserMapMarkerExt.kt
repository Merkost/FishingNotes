package com.mobileprism.fishing.domain.entity.content

import com.google.android.gms.maps.model.LatLng

val UserMapMarker.latLng: LatLng get() = LatLng(latitude, longitude)
