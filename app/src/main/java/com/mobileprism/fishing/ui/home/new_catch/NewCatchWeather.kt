package com.mobileprism.fishing.ui.home.new_catch

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
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.NewCatchViewModel
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.mappers.getMoonIconByPhase
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.model.mappers.getWeatherNameByIcon
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import com.mobileprism.fishing.utils.time.toHours
import org.koin.androidx.compose.get
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WeatherLayout(
    weatherForecast: WeatherForecast?,
    viewModel: NewCatchViewModel,
) {
    val calendar = Calendar.getInstance()
    val weatherSettingsImpl: WeatherPreferencesImpl = get()
    val temperatureSettings by weatherSettingsImpl.getTemperatureUnit.collectAsState(
        TemperatureValues.C
    )
    val pressureUnit by weatherSettingsImpl.getPressureUnit.collectAsState(PressureValues.mmHg)

    weatherForecast?.let { weather ->

        var weatherIconDialogState by remember {
            mutableStateOf(false)
        }

        val currentMoonPhase = remember {
            weather.daily.first().moonPhase
        }

//        viewModel.moonPhase.value = calcMoonPhase(
//            currentMoonPhase,
//            Date().time / MILLISECONDS_IN_SECOND,
//            weather.hourly.first().date
//        )

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
            mutableStateOf(pressureUnit.getPressure(weather.hourly[hour].pressure))
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
                val windDeg by remember(hour, animatedWeather) {
                    mutableStateOf(animatedWeather.hourly[hour].windDeg.toFloat())
                }

                LaunchedEffect(key1 = windDeg) {
                    viewModel.weatherToSave.value.windDirInDeg = windDeg
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        readOnly = false,
                        value = wind,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.rotate(windDeg),
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

@ExperimentalComposeUiApi
@Composable
fun NewCatchWeatherPrimary(
    modifier: Modifier = Modifier,
    weatherDescription: String,
    weatherIconId: String,
    onDescriptionChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onError: (Boolean) -> Unit
) {
    var weatherIconDialogState by remember { mutableStateOf(false) }

    if (weatherIconDialogState) {
        PickWeatherIconDialog(
            onDismiss = { weatherIconDialogState = false },
            onIconSelected = {
                onIconChange(getWeatherNameByIcon(it))
                weatherIconDialogState = false
            })
    }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = false,
        value = weatherDescription,
        leadingIcon = {
            IconButton(
                modifier = Modifier.size(32.dp),
                content = {
                    Icon(
                        painter = painterResource(id = getWeatherIconByName(weatherIconId)),
                        contentDescription = "",
                        tint = Color.Unspecified
                    )
                },
                onClick = { weatherIconDialogState = true }
            )
        },
        onValueChange = { onDescriptionChange(it) },
        isError = (weatherDescription.isEmpty()).apply { onError(this) },
        label = { Text(text = stringResource(id = R.string.weather)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@Composable
fun NewCatchTemperatureView(
    modifier: Modifier = Modifier,
    temperature: String,
    onTemperatureChange: (String) -> Unit,
    onError: (Boolean) -> Unit
) {

    val weatherSettings: WeatherPreferences = get()
    val temperatureUnit by weatherSettings.getTemperatureUnit().collectAsState(TemperatureValues.C)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = false,
        value = temperature,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_thermometer),
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
        },
        trailingIcon = { Text(text = stringResource(temperatureUnit.stringRes)) },
        onValueChange = { onTemperatureChange(it) },
        isError = (temperature
            .toIntOrNull() == null || temperature.length > 3)
            .apply { onError(this) },
        label = { Text(text = stringResource(R.string.temperature)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

@Composable
fun NewCatchPressureView(
    modifier: Modifier = Modifier,
    pressure: String,
    onPressureChange: (String) -> Unit,
    onError: (Boolean) -> Unit
) {
    val weatherSettings: WeatherPreferences = get()
    val pressureUnit by weatherSettings.getPressureUnit().collectAsState(PressureValues.mmHg)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = false,
        value = pressure,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_gauge),
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
        },
        trailingIcon = { Text(text = stringResource(pressureUnit.stringRes)) },
        isError = (pressure.toDoubleOrNull() == null || pressure.endsWith(".") || pressure
            .isEmpty())
            .apply { onError(this) },
        onValueChange = { onPressureChange(it) },
        label = { Text(text = stringResource(R.string.pressure)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewCatchWindView(
    modifier: Modifier = Modifier,
    wind: String,
    windDeg: Int,
    onWindChange: (String) -> Unit,
    onWindDirChange: (Float) -> Unit,
    onError: (Boolean) -> Unit
) {
    val weatherSettings: WeatherPreferences = get()
    val windSpeedUnit by weatherSettings.getWindSpeedUnit().collectAsState(WindSpeedValues.kmph)

    var windDirDialogState by remember { mutableStateOf(false) }

    if (windDirDialogState) {
        PickWindDirDialog(
            onDirectionSelected = {
                onWindDirChange(it)
                windDirDialogState = false
            },
            onDismiss = { windDirDialogState = false }
        )
    }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = false,
        value = wind,
        leadingIcon = {
            IconButton(onClick = { windDirDialogState = true }) {
                Icon(
                    modifier = Modifier.rotate(windDeg.toFloat()),
                    painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary,
                )
            }

        },
        trailingIcon = { Text(text = stringResource(windSpeedUnit.stringRes)) },
        onValueChange = { onWindChange(it) },
        isError = (wind
            .toIntOrNull() == null || wind.length >= 3)
            .apply { onError(this) },
        label = { Text(text = stringResource(R.string.wind)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

@Composable
fun NewCatchMoonView(
    modifier: Modifier = Modifier,
    moonPhase: Float,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        value = (moonPhase * 100).toInt().toString(),
        leadingIcon = {
            Icon(
                painter = painterResource(
                    id = getMoonIconByPhase(moonPhase)
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
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}