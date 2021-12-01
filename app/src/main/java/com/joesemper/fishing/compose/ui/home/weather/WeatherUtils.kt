package com.joesemper.fishing.compose.ui.home.weather

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily

fun createCurrentPlaceItem(latLng: LatLng, context: Context): UserMapMarker {
    return UserMapMarker(
        title = context.getString(R.string.current_location),
        latitude = latLng.latitude,
        longitude = latLng.longitude
    )
}

fun getPressureList(
    forecast: List<Daily>,
    pressureUnit: String
): List<Int> {
    return forecast.map { getPressureInt(it.pressure, PressureValues.valueOf(pressureUnit)) }
}

fun getBounds(list: List<Int>): Pair<Int, Int> {
    var min = Int.MAX_VALUE
    var max = -Int.MAX_VALUE
    list.forEach {
        min = min.coerceAtMost(it)
        max = max.coerceAtLeast(it)
    }
    return Pair(min, max)
}

data class Point(
    val x: Float,
    val y: Float
)


@Composable
fun getTemperatureFromUnit(temperatureUnit: String): String {
    return when (temperatureUnit) {
        TemperatureValues.C.name -> stringResource(R.string.celsius)
        TemperatureValues.F.name -> stringResource(R.string.fahrenheit)
        TemperatureValues.K.name -> stringResource(R.string.kelvin)
        else -> {""}
    }
}

fun getPressure(hPa: Int, pressureValue: PressureValues): String {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100).toString()
        PressureValues.Bar -> (hPa / 1000f).toString()
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt().toString()
        PressureValues.Psi -> String.format("%.5g", (hPa * 0.0145037738))
    }
}

fun getPressureInt(hPa: Int, pressureValue: PressureValues): Int {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100)
        PressureValues.Bar -> hPa
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt()
        PressureValues.Psi -> (hPa * 0.0145037738 * 100).toInt()
    }
}


fun getTemperature(temperature: Float, temperatureValue: TemperatureValues): String {
    return when (temperatureValue) {
        TemperatureValues.C -> temperature.toInt().toString()
        TemperatureValues.F -> (temperature * 9f/5f + 32).toInt().toString()
        TemperatureValues.K -> (temperature + 273.15).toInt().toString()
    }
}

enum class PressureValues {
    Pa,
    Bar,
    mmHg,
    Psi
}

enum class TemperatureValues {
    C, F, K
}