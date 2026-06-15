package com.mobileprism.fishing.ui.home.weather

import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.Hourly
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.utils.Constants.CURRENT_PLACE_ITEM_ID
import org.jetbrains.compose.resources.StringResource
import kotlin.math.abs

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
    return forecast.map { pressureUnit.getPressureFromHpa(it.pressure).toDoubleOrNull()?.toInt() ?: it.pressure }
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

enum class PressureDirection { RISING, FALLING, STEADY }

data class PressureTrend(val deltaHpa: Int, val direction: PressureDirection)

fun pressureTrend(hourly: List<Hourly>): PressureTrend? {
    if (hourly.size < 4) return null
    val delta = hourly[0].pressure - hourly[3].pressure
    val direction = when {
        abs(delta) <= 1 -> PressureDirection.STEADY
        delta > 0 -> PressureDirection.RISING
        else -> PressureDirection.FALLING
    }
    return PressureTrend(deltaHpa = delta, direction = direction)
}

enum class MoonPhase {
    NEW,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT,
}

fun moonPhaseName(phase: Float): MoonPhase {
    val p = phase.coerceIn(0f, 1f)
    return when {
        p <= 0.02f -> MoonPhase.NEW
        p < 0.25f -> MoonPhase.WAXING_CRESCENT
        p <= 0.27f -> MoonPhase.FIRST_QUARTER
        p < 0.48f -> MoonPhase.WAXING_GIBBOUS
        p <= 0.52f -> MoonPhase.FULL
        p < 0.73f -> MoonPhase.WANING_GIBBOUS
        p <= 0.77f -> MoonPhase.LAST_QUARTER
        p < 0.98f -> MoonPhase.WANING_CRESCENT
        else -> MoonPhase.NEW
    }
}

enum class BiteRating { POOR, FAIR, GOOD, EXCELLENT }

enum class BiteFactorKey {
    FALLING_PRESSURE,
    RISING_PRESSURE,
    STEADY_PRESSURE,
    OVERCAST,
    CALM_WIND,
    STRONG_WIND,
    LOW_LIGHT,
}

data class BiteForecast(
    val score: Int,
    val rating: BiteRating,
    val topFactors: List<BiteFactorKey>,
)

private const val SECONDS_PER_HOUR = 3600L
private const val LOW_LIGHT_WINDOW_SECONDS = (1.5 * SECONDS_PER_HOUR).toLong()

fun biteForecast(hourly: List<Hourly>, daily: Daily?): BiteForecast? {
    val current = hourly.firstOrNull() ?: return null

    var score = 50
    val contributions = mutableListOf<Pair<BiteFactorKey, Int>>()

    val trend = pressureTrend(hourly)
    when (trend?.direction) {
        PressureDirection.FALLING -> {
            val delta = abs(trend.deltaHpa).coerceAtMost(6)
            val weight = 8 + delta * 3
            score += weight
            contributions += BiteFactorKey.FALLING_PRESSURE to weight
        }
        PressureDirection.RISING -> {
            val delta = abs(trend.deltaHpa).coerceAtMost(6)
            val weight = -(6 + delta * 3)
            score += weight
            contributions += BiteFactorKey.RISING_PRESSURE to weight
        }
        PressureDirection.STEADY -> {
            val weight = 4
            score += weight
            contributions += BiteFactorKey.STEADY_PRESSURE to weight
        }
        null -> {}
    }

    val clouds = current.clouds
    if (clouds in 40..80) {
        val weight = 12
        score += weight
        contributions += BiteFactorKey.OVERCAST to weight
    }

    val wind = current.windSpeed
    when {
        wind < 5f -> {
            val weight = 10
            score += weight
            contributions += BiteFactorKey.CALM_WIND to weight
        }
        wind > 9f -> {
            val weight = -((wind - 9f).coerceAtMost(6f) * 3f).toInt() - 6
            score += weight
            contributions += BiteFactorKey.STRONG_WIND to weight
        }
    }

    if (daily != null && current.date != 0L) {
        val nearSunrise = daily.sunrise != 0L &&
            abs(current.date - daily.sunrise) <= LOW_LIGHT_WINDOW_SECONDS
        val nearSunset = daily.sunset != 0L &&
            abs(current.date - daily.sunset) <= LOW_LIGHT_WINDOW_SECONDS
        if (nearSunrise || nearSunset) {
            val weight = 15
            score += weight
            contributions += BiteFactorKey.LOW_LIGHT to weight
        }
    }

    val clamped = score.coerceIn(0, 100)
    val rating = when {
        clamped < 35 -> BiteRating.POOR
        clamped < 55 -> BiteRating.FAIR
        clamped < 75 -> BiteRating.GOOD
        else -> BiteRating.EXCELLENT
    }

    val topFactors = contributions
        .sortedByDescending { abs(it.second) }
        .take(2)
        .map { it.first }

    return BiteForecast(score = clamped, rating = rating, topFactors = topFactors)
}
