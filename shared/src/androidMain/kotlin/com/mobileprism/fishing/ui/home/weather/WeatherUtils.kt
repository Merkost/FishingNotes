package com.mobileprism.fishing.ui.home.weather

import android.content.Context
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.utils.Constants.CURRENT_PLACE_ITEM_ID

val PressureValues.stringRes: Int get() = when (this) {
    PressureValues.Pa -> R.string.pressure_pa
    PressureValues.Bar -> R.string.pressure_bar
    PressureValues.mmHg -> R.string.pressure_mm
    PressureValues.Psi -> R.string.pressure_psi
    PressureValues.Hpa -> R.string.pressure_hpa
}

val TemperatureValues.stringRes: Int get() = when (this) {
    TemperatureValues.C -> R.string.celsius
    TemperatureValues.F -> R.string.fahrenheit
    TemperatureValues.K -> R.string.kelvin
}

val WindSpeedValues.stringRes: Int get() = when (this) {
    WindSpeedValues.metersps -> R.string.wind_mps
    WindSpeedValues.milesph -> R.string.wind_mph
    WindSpeedValues.knots -> R.string.wind_knots
    WindSpeedValues.ftps -> R.string.wind_ftps
    WindSpeedValues.kmph -> R.string.wind_kmph
}

fun createCurrentPlaceItem(latLng: LatLng, context: Context): UserMapMarker {
    return UserMapMarker(
        id = CURRENT_PLACE_ITEM_ID,
        title = context.getString(R.string.current_location),
        latitude = latLng.latitude,
        longitude = latLng.longitude
    )
}

fun getPressureList(
    forecast: List<Daily>,
    pressureUnit: PressureValues
): List<Int> {
    return forecast.map { /*pressureUnit.getPressureInt(it.pressure)*/it.pressure.toInt() }
}

fun getPrecipitationList(forecast: List<Daily>): List<Int> {
    return forecast.map { (it.probabilityOfPrecipitation * 100).toInt() }
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


fun navigateToDailyWeatherScreen(
    navController: NavController,
    index: Int,
    forecastDaily: List<Daily>
) {
    val argument = DailyWeatherData(
        selectedDay = index,
        dailyForecast = forecastDaily
    )
    navController.navigate(MainDestinations.DailyWeather(argument))
}

fun navigateToAddNewPlace(navController: NavController) {
    navController.navigate(MainDestinations.Map(isAddingNewPlace = true))
}