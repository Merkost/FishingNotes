package com.mobileprism.fishing.ui.home.weather

import androidx.compose.runtime.Composable
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.weather_label_with_unit
import fishing.shared.generated.resources.weather_percent_value
import fishing.shared.generated.resources.weather_value_with_unit
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
