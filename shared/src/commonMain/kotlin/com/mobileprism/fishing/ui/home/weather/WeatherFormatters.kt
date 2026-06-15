package com.mobileprism.fishing.ui.home.weather

import androidx.compose.runtime.Composable
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun probabilityToPercent(probability: Float): Int = (probability * 100).toInt()

fun moonPhaseToPercent(moonPhase: Float): Int = (moonPhase * 100).toInt()

fun isHeavyPrecipitation(probability: Float): Boolean = probability >= 0.2f

@Composable
fun labelWithUnit(labelRes: StringResource, unitRes: StringResource): String =
    stringResource(Res.string.weather_label_with_unit, stringResource(labelRes), stringResource(unitRes))

@Composable
fun valueWithUnit(value: String, unitRes: StringResource): String =
    stringResource(Res.string.weather_value_with_unit, value, stringResource(unitRes))

@Composable
fun percentText(percent: Int): String =
    stringResource(Res.string.weather_percent_value, percent)

@Composable
fun temperatureText(unit: TemperatureValues, temperature: Float): String =
    unit.getTemperature(temperature) + stringResource(unit.stringRes)

@Composable
fun pressureText(unit: PressureValues, hPa: Int): String =
    valueWithUnit(unit.getPressureFromHpa(hPa), unit.stringRes)

@Composable
fun windSpeedText(unit: WindSpeedValues, windSpeed: Double): String =
    valueWithUnit(unit.getDefaultWindSpeed(windSpeed), unit.stringRes)

fun biteRatingStringRes(rating: BiteRating): StringResource = when (rating) {
    BiteRating.POOR -> Res.string.bite_poor
    BiteRating.FAIR -> Res.string.bite_fair
    BiteRating.GOOD -> Res.string.bite_good
    BiteRating.EXCELLENT -> Res.string.bite_excellent
}

fun biteFactorStringRes(factor: BiteFactorKey): StringResource = when (factor) {
    BiteFactorKey.FALLING_PRESSURE -> Res.string.bite_factor_falling_pressure
    BiteFactorKey.RISING_PRESSURE -> Res.string.bite_factor_rising_pressure
    BiteFactorKey.STEADY_PRESSURE -> Res.string.bite_factor_steady_pressure
    BiteFactorKey.OVERCAST -> Res.string.bite_factor_overcast
    BiteFactorKey.CALM_WIND -> Res.string.bite_factor_calm_wind
    BiteFactorKey.STRONG_WIND -> Res.string.bite_factor_strong_wind
    BiteFactorKey.LOW_LIGHT -> Res.string.bite_factor_low_light
}

fun pressureDirectionStringRes(direction: PressureDirection): StringResource = when (direction) {
    PressureDirection.RISING -> Res.string.pressure_rising
    PressureDirection.FALLING -> Res.string.pressure_falling
    PressureDirection.STEADY -> Res.string.pressure_steady
}

fun moonPhaseStringRes(phase: MoonPhase): StringResource = when (phase) {
    MoonPhase.NEW -> Res.string.moon_new
    MoonPhase.WAXING_CRESCENT -> Res.string.moon_waxing_crescent
    MoonPhase.FIRST_QUARTER -> Res.string.moon_first_quarter
    MoonPhase.WAXING_GIBBOUS -> Res.string.moon_waxing_gibbous
    MoonPhase.FULL -> Res.string.moon_full
    MoonPhase.WANING_GIBBOUS -> Res.string.moon_waning_gibbous
    MoonPhase.LAST_QUARTER -> Res.string.moon_last_quarter
    MoonPhase.WANING_CRESCENT -> Res.string.moon_waning_crescent
}
