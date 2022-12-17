package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.entity.FishingWeather
import com.mobileprism.fishing.ui.custom.DefaultDialog


@Composable
fun PickWeatherIconDialog(onWeatherSelected: (FishingWeather) -> Unit, onDismiss: () -> Unit) {
    DefaultDialog(
        stringResource(R.string.choose_weather),
        content = { WeatherTypesSheet(onWeatherSelected = onWeatherSelected) },
        onDismiss = onDismiss
    )
}

@Composable
fun WeatherTypesSheet(onWeatherSelected: (FishingWeather) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 35.dp)
            .verticalScroll(rememberScrollState())
    ) {
        WeatherType(
            weatherList = FishingWeather.values(),
            onWeatherSelected = onWeatherSelected
        )
    }
}

@Composable
fun WeatherType(
    weatherList: Array<FishingWeather>,
    onWeatherSelected: (FishingWeather) -> Unit,
) {
    Column {
        weatherList.forEach {
            WeatherTypeItem(it, onWeatherSelected)
        }
    }
}

@Composable
fun WeatherTypeItem(
    fishingWeather: FishingWeather,
    onWeatherSelected: (FishingWeather) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .clickable { onWeatherSelected(fishingWeather) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = stringResource(id = fishingWeather.stringRes)
        )
    }
}