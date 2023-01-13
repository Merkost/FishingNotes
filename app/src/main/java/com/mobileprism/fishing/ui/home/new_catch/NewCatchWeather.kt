package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.FishingWeather
import com.mobileprism.fishing.ui.home.new_catch.weather.*
import org.koin.androidx.compose.get


@Composable
fun NewCatchWeatherPrimary(
    modifier: Modifier = Modifier,
    fishingWeather: FishingWeather,
    onWeatherChange: (FishingWeather) -> Unit,
) {
    var weatherIconDialogState by remember { mutableStateOf(false) }

    if (weatherIconDialogState) {
        PickWeatherIconDialog(
            onDismiss = { weatherIconDialogState = false },
            onWeatherSelected = {
                onWeatherChange(it)
                weatherIconDialogState = false
            })
    }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (it.hasFocus) { weatherIconDialogState = true } },
        readOnly = true,
        value = stringResource(id = fishingWeather.stringRes),
        leadingIcon = {
            IconButton(
                modifier = Modifier.size(32.dp),
                content = {
                    Icon(
                        painter = painterResource(id = fishingWeather.iconRes),
                        contentDescription = "",
                        tint = Color.Unspecified
                    )
                },
                onClick = { weatherIconDialogState = true }
            )
        },
        onValueChange = {  },
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
        label = { Text(text = stringResource(R.string.temperature)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}