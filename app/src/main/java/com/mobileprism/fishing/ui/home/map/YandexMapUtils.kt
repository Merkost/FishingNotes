package com.mobileprism.fishing.ui.home.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.yandex.mapkit.mapview.MapView as YandexMapView

object YandexMapTypes {
    val none = MapType.NONE
    val roadmap = MapType.MAP
    val satellite = MapType.SATELLITE
    val hybrid = MapType.HYBRID
    val terrain = MapType.VECTOR_MAP
}

fun moveCameraToLocation(
    coroutineScope: CoroutineScope,
    mapView: YandexMapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM,
    bearing: Float = 0f
) {
    coroutineScope.launch {
        val yandexMap = mapView.map
        yandexMap.move(
            CameraPosition(Point(location.latitude, location.longitude), zoom, bearing, 0f),
            Animation(Animation.Type.SMOOTH, 1F), null
        )
    }
}

fun setCameraPosition(
    coroutineScope: CoroutineScope,
    mapView: YandexMapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM,
    bearing: Float = DEFAULT_BEARING
) {
    coroutineScope.launch {
        val yandexMap = mapView.map
        yandexMap.move(
            CameraPosition(Point(location.latitude, location.longitude), zoom, bearing, 0f),
        )
    }
}