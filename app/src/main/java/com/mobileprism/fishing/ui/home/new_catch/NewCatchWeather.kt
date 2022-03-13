package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.mappers.getMoonIconByPhase
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.model.mappers.getWeatherNameByIcon
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.new_catch.weather.*
import com.mobileprism.fishing.ui.home.views.WeatherIconItem
import com.mobileprism.fishing.ui.utils.enums.StringOperation
import com.mobileprism.fishing.ui.utils.toDoubleExOrNull
import com.mobileprism.fishing.ui.viewmodels.NewCatchViewModel
import com.mobileprism.fishing.utils.time.toHours
import org.koin.androidx.compose.get
import java.util.*

@Composable
fun WeatherTypesSheet(onWeatherSelected: (SelectedWeather) -> Unit) {
    val weatherTypes = listOf(
        Atmosphere.values(),
        Clear.values(),
        Clouds.values(),
        Drizzle.values(),
        Rain.values(),
        Snow.values(),
        Thunderstorm.values()
    )

    var openedList: Array<out Enum<*>>? by remember {
        mutableStateOf(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 35.dp)
            .verticalScroll(rememberScrollState())
    ) {
        weatherTypes.forEach {
            WeatherType(
                it,
                isOpened = openedList?.javaClass == it.javaClass,
                onWeatherSelected = onWeatherSelected
            ) {
                openedList =
                    if (openedList == null || openedList?.javaClass != it.javaClass) it else null
            }
        }
    }
}

@Composable
fun <T> WeatherType(
    it: Array<out T>,
    isOpened: Boolean,
    onWeatherSelected: (SelectedWeather) -> Unit,
    onWeatherTypeClicked: () -> Unit
) where T : StringOperation, T : WeatherIconPrefix {
    WeatherTypeTitle(it, isOpened = isOpened, onWeatherTypeClicked)
    AnimatedVisibility(visible = isOpened) {
        Column {
            it.forEach {
                WeatherTypeItem(it, onWeatherSelected)
            }
        }
    }
}

@Composable
fun <T> WeatherTypeTitle(
    param: Array<out T>,
    isOpened: Boolean,
    onWeatherTypeClicked: () -> Unit
) where T : StringOperation, T : WeatherIconPrefix {
    val angle by animateFloatAsState(
        when (isOpened) {
            true -> 180f
            else -> 0f
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWeatherTypeClicked() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeatherIconItem(getWeatherIconByName(param.first().iconPrefix))
        Text(text = stringResource(id = param.first().getNameRes))
        IconButton(onClick = onWeatherTypeClicked ) {
            Icon(Icons.Default.ArrowDropDown, "", modifier = Modifier.rotate(angle))
        }
    }
}

@Composable
fun <T> WeatherTypeItem(it: T, onWeatherSelected: (SelectedWeather) -> Unit) where T : StringOperation, T : WeatherIconPrefix {
    Box(modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 40.dp).padding(4.dp)
        .clickable { onWeatherSelected(SelectedWeather(it.stringRes, it.iconPrefix)) }) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(id = it.stringRes))
        }
    }
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
    val context = LocalContext.current
    var weatherIconDialogState by remember { mutableStateOf(false) }

    if (weatherIconDialogState) {
        PickWeatherIconDialog(
            onDismiss = { weatherIconDialogState = false },
            onWeatherSelected = {
                onIconChange(it.iconPrefix)
                onDescriptionChange(context.getString(it.stringRes))
                weatherIconDialogState = false
            })
    }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth().onFocusChanged {
             if (it.hasFocus) { weatherIconDialogState = true }
        },
        readOnly = true,
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
    val temperatureUnit by weatherSettings.getTemperatureUnit.collectAsState(TemperatureValues.C)

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
    val pressureUnit by weatherSettings.getPressureUnit.collectAsState(PressureValues.mmHg)

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
    val windSpeedUnit by weatherSettings.getWindSpeedUnit.collectAsState(WindSpeedValues.kmph)

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
        isError = (wind.toDoubleExOrNull() == null || wind.length > 4).apply { onError(this) },
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