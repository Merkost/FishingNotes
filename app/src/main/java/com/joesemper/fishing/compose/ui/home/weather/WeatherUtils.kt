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

fun getPressure(hPa: Int, pressureValue: PressureValues): String {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100).toString()
        PressureValues.bar -> (hPa / 1000f).toString()
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt().toString()
        PressureValues.psi -> (hPa * 0.0145037738).toString()
        else -> {
            (hPa * 0.75006375541921).toInt().toString()
        }
    }
}

fun getPressureInt(hPa: Int, pressureValue: PressureValues): Int {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100)
        PressureValues.bar -> (hPa / 1000f).toInt()
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt()
        PressureValues.psi -> (hPa * 0.0145037738).toInt()
        else -> {
            (hPa * 0.75006375541921).toInt().toInt()
        }
    }
}

enum class PressureValues {
    Pa,
    bar,
    mmHg,
    psi
}