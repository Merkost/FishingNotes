package com.mobileprism.fishing.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.R
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.map.DEFAULT_ZOOM
import kotlin.math.pow
import kotlin.math.sqrt

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun showErrorToast(context: Context, text: String? = null) {
    Toast.makeText(context, text ?: context.getString(R.string.error_occured), Toast.LENGTH_SHORT).show()
}

fun getCameraPosition(latLng: LatLng): Pair<LatLng, Float> {
    val lat = latLng.latitude + ((-100..100).random() * 0.000000001)
    val lng = latLng.longitude + ((-100..100).random() * 0.000000001)
    return Pair(LatLng(lat, lng), DEFAULT_ZOOM)
}

fun isCoordinatesFar(first: LatLng, second: LatLng): Boolean {
    return (sqrt(
        ((first.latitude - second.latitude).pow(2))
                + ((first.longitude - second.longitude).pow(2))
    ) > 0.1)
}
