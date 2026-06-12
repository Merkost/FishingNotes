package com.mobileprism.fishing.ui.home.weather

import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.utils.Constants.CURRENT_PLACE_ITEM_ID
import org.jetbrains.compose.resources.StringResource

val PressureValues.stringRes: StringResource get() = when (this) {
    PressureValues.Pa -> Res.string.pressure_pa
    PressureValues.Bar -> Res.string.pressure_bar
    PressureValues.mmHg -> Res.string.pressure_mm
    PressureValues.Psi -> Res.string.pressure_psi
    PressureValues.Hpa -> Res.string.pressure_hpa
}

val TemperatureValues.stringRes: StringResource get() = when (this) {
    TemperatureValues.C -> Res.string.celsius
    TemperatureValues.F -> Res.string.fahrenheit
    TemperatureValues.K -> Res.string.kelvin
}

val WindSpeedValues.stringRes: StringResource get() = when (this) {
    WindSpeedValues.metersps -> Res.string.wind_mps
    WindSpeedValues.milesph -> Res.string.wind_mph
    WindSpeedValues.knots -> Res.string.wind_knots
    WindSpeedValues.ftps -> Res.string.wind_ftps
    WindSpeedValues.kmph -> Res.string.wind_kmph
}

fun createCurrentPlaceItem(latitude: Double, longitude: Double, title: String): UserMapMarker {
    return UserMapMarker(
        id = CURRENT_PLACE_ITEM_ID,
        title = title,
        latitude = latitude,
        longitude = longitude
    )
}

fun getPressureList(
    forecast: List<Daily>,
    pressureUnit: PressureValues
): List<Int> {
    return forecast.map { it.pressure }
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
