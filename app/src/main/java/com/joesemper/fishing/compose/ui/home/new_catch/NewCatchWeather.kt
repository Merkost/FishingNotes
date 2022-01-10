package com.joesemper.fishing.compose.ui.home.new_catch

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.model.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.views.SecondaryText
import com.joesemper.fishing.compose.ui.home.weather.PressureValues
import com.joesemper.fishing.compose.ui.home.weather.TemperatureValues
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.model.mappers.getWeatherNameByIcon
import com.joesemper.fishing.utils.calcMoonPhase
import com.joesemper.fishing.utils.time.TimeConstants.MILLISECONDS_IN_SECOND
import com.joesemper.fishing.utils.time.toHours
import org.koin.androidx.compose.get
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WeatherLayout(
    weatherForecast: WeatherForecast?,
    viewModel: NewCatchViewModel,
) {
    val calendar = Calendar.getInstance()
    val weatherSettings: WeatherPreferences = get()
    val temperatureSettings by weatherSettings.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val pressureUnit by weatherSettings.getPressureUnit.collectAsState(PressureValues.mmHg)

    weatherForecast?.let { weather ->

        var weatherIconDialogState by remember {
            mutableStateOf(false)
        }

        val currentMoonPhase = remember {
            weather.daily.first().moonPhase
        }
        viewModel.moonPhase.value = calcMoonPhase(
            currentMoonPhase,
            Date().time / MILLISECONDS_IN_SECOND,
            weather.hourly.first().date
        )

        val hour by remember(calendar.timeInMillis) {
            mutableStateOf(calendar.timeInMillis.toHours().toInt())
        }

        var weatherIcon by remember(hour, weather) {
            mutableStateOf(getWeatherIconByName(weather.hourly.first().weather.first().icon))
        }.also { viewModel.weatherToSave.value.icon = getWeatherNameByIcon(it.value) }

        var weatherDescription by remember(hour, weather) {
            mutableStateOf(weather.hourly[hour].weather
                .first().description.replaceFirstChar { it.uppercase() })
        }.also { viewModel.weatherToSave.value.weatherDescription = it.value }

        var temperature by remember(hour, weather, temperatureSettings) {
            mutableStateOf(viewModel.getTemperatureForHour(hour = hour, temperatureSettings))
        }.also {
            it.value.toFloatOrNull()?.let { floatValue ->
                viewModel.weatherToSave.value.temperatureInC =
                    temperatureSettings.getCelciusTemperature(floatValue)
            }
        }

        var pressure by remember(hour, weather, pressureUnit) {
            mutableStateOf(pressureUnit.getPressure(weather.hourly[hour].pressure,))
        }.also {
            it.value.toFloatOrNull()?.let { floatValue ->
                viewModel.weatherToSave.value.pressureInMmhg =
                    pressureUnit.getDefaultPressure(floatValue)
            }
        }

        var wind by remember(hour, weather) {
            mutableStateOf(weather.hourly[hour].windSpeed.toInt().toString())
        }.also {
            it.value.toIntOrNull()?.let { intValue ->
                viewModel.weatherToSave.value.windInMs = intValue
            }
        }


        if (weatherIconDialogState) PickWeatherIconDialog(
            onDismiss = { weatherIconDialogState = false },
            onIconSelected = {
                weatherIcon = it
                weatherIconDialogState = false
            })

        Crossfade(targetState = weather) { animatedWeather ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                //Main weather title
                OutlinedTextField(
                    readOnly = false,
                    value = weatherDescription,
                    leadingIcon = {
                        IconButton(
                            modifier = Modifier.size(32.dp),
                            content = {
                                Icon(
                                    painter = painterResource(id = weatherIcon),
                                    contentDescription = "",
                                    tint = Color.Unspecified
                                )
                            },
                            onClick = { weatherIconDialogState = true }
                        )
                    },
                    onValueChange = { weatherDescription = it },
                    isError = (weatherDescription.isEmpty()).apply {
                        viewModel.noErrors.value = this
                    },
                    label = { Text(text = stringResource(id = R.string.weather)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                //Temperature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        readOnly = false,
                        value = temperature,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_thermometer),
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary
                            )
                        },
                        trailingIcon = {
                            Text(text = stringResource(temperatureSettings.stringRes))
                        },
                        onValueChange = { newValue ->
                            temperature = newValue
                            /*temperature = when (newValue.toIntOrNull()) {
                                null -> temperature
                                //old value
                                else -> newValue.toInt().let {
                                    if (it in -300..300) newValue else temperature   //new value
                                }
                            }*/
                        },
                        isError = (temperature.toIntOrNull() == null || temperature.length > 3)
                            .apply { viewModel.noErrors.value = this },
                        label = { Text(text = stringResource(R.string.temperature)) },
                        modifier = Modifier.weight(1f, true),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )

                    //Pressure
                    OutlinedTextField(
                        readOnly = false,
                        value = pressure,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_gauge),
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary
                            )
                        },
                        trailingIcon = {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = stringResource(pressureUnit.stringRes)
                            )
                        },
                        isError = (pressure.endsWith(".") || pressure.isEmpty() || pressure.toDoubleOrNull() == null)
                            .apply { viewModel.noErrors.value = this },
                        onValueChange = { pressure = it },
                        label = { Text(text = stringResource(R.string.pressure)) },
                        modifier = Modifier.weight(1f, true),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }

                //Wind and Moon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        readOnly = false,
                        value = wind,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.rotate(animatedWeather.hourly[hour].windDeg.toFloat()),
                                painter = painterResource(id = R.drawable.ic_arrow_up),
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary,
                            )
                        },
                        trailingIcon = { Text(text = stringResource(R.string.wind_speed_units)) },
                        onValueChange = { wind = it },
                        isError = (wind.toIntOrNull() == null || wind.length >= 3)
                            .apply { viewModel.noErrors.value = this },
                        label = { Text(text = stringResource(R.string.wind)) },
                        modifier = Modifier.weight(1f, true),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        readOnly = true,
                        value = (viewModel.moonPhase.value * 100).toInt().toString(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    id = getMoonIconByPhase(viewModel.moonPhase.value)
                                ),
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary
                            )
                        },
                        onValueChange = { },
                        trailingIcon = {
                            Text(text = stringResource(R.string.percent))
                        },
                        label = { Text(text = stringResource(R.string.moon_phase)) },
                        modifier = Modifier.weight(1f, true),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }
            }
        }
    } ?: SecondaryText(
        modifier = Modifier.padding(8.dp),
        text = stringResource(R.string.select_place_for_weather)
    )
}

@Composable
fun WeatherLayoutLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) { CircularProgressIndicator() }
}