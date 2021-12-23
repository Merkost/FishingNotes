package com.joesemper.fishing.compose.ui.home.weather

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.utils.hPaToMmHg

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

fun getTemperature(temperature: Float, temperatureValue: TemperatureValues): String {
    return when (temperatureValue) {
        TemperatureValues.C -> temperature.toInt().toString()
        TemperatureValues.F -> (temperature * 9f/5f + 32).toInt().toString()
        TemperatureValues.K -> (temperature + 273.15).toInt().toString()
    }
}

fun getCelciusTemperature(temperaturInC: Float, from: String): Int {
    return when (from) {
        TemperatureValues.C.name -> temperaturInC.toInt()
        TemperatureValues.F.name -> ((temperaturInC - 32)*(5/9)).toInt()
        TemperatureValues.K.name -> (temperaturInC - 273.15).toInt()
        else -> {temperaturInC.toInt()}
    }
}

@Composable
fun getTemperatureNameFromUnit(temperatureUnit: String): String {
    return when (temperatureUnit) {
        TemperatureValues.C.name -> stringResource(R.string.celsius)
        TemperatureValues.F.name -> stringResource(R.string.fahrenheit)
        TemperatureValues.K.name -> stringResource(R.string.kelvin)
        else -> { stringResource(R.string.celsius) }
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

/*
    Used for weather pressure chart
 */
fun getPressureInt(hPa: Int, pressureValue: PressureValues): Int {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100)
        PressureValues.Bar -> hPa
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt()
        PressureValues.Psi -> (hPa * 0.0145037738 * 100).toInt()
    }
}

fun getDefaultPressure(value: Float, from: String): Int {
    return when (from) {
        PressureValues.Pa.name -> (value * 0.0075006156130264f).toInt()
        PressureValues.Bar.name -> (value * 750.06168f).toInt()
        PressureValues.mmHg.name -> value.toInt()
        PressureValues.Psi.name -> (value * 51.71484f).toInt()
        else -> { value.toInt() }
    }
}

@Composable
fun getPressureNameFromUnit(pressureUnit: String): String {
    return when (pressureUnit) {
        PressureValues.mmHg.name -> stringResource(R.string.pressure_mm)
        PressureValues.Pa.name -> stringResource(R.string.pressure_pa)
        PressureValues.Bar.name -> stringResource(R.string.pressure_bar)
        PressureValues.Psi.name -> stringResource(R.string.pressure_psi)
        else -> {
            stringResource(R.string.pressure_mm)
        }
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

fun navigateToDailyWeatherScreen(
    navController: NavController,
    index: Int,
    forecastDaily: List<Daily>
) {
    val argument = DailyWeatherData(
        selectedDay = index,
        dailyForecast = forecastDaily
    )
    navController.navigate(
        MainDestinations.DAILY_WEATHER_ROUTE,
        Arguments.WEATHER_DATA to argument
    )
}