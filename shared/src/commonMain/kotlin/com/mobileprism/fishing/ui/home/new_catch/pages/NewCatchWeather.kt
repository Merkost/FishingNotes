package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.home.new_catch.*
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.rememberConnectionState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.catchWeatherState.collectAsState()

    val internetConnectionState by rememberConnectionState()

    var primaryWeatherError by remember { mutableStateOf(false) }
    var temperatureError by remember { mutableStateOf(false) }
    var pressureError by remember { mutableStateOf(false) }
    var windError by remember { mutableStateOf(false) }

    val isError1 by remember(primaryWeatherError, temperatureError) {
        mutableStateOf(primaryWeatherError || temperatureError)
    }
    val isError2 by remember(pressureError, windError) {
        mutableStateOf(pressureError || windError)
    }

    LaunchedEffect(key1 = isError1, isError2) {
        viewModel.setWeatherIsError(isError1 || isError2)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubtitleWithIcon(
                icon = Res.drawable.weather_cloudy,
                text = stringResource(Res.string.weather)
            )

            if (internetConnectionState is ConnectionState.Unavailable) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp),
                    painter = painterResource(Res.drawable.ic_no_internet),
                    contentDescription = null,
                    tint = MaterialTheme.customColors.secondaryIconColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    when {
                        state.isDownloadAvailable
                                && internetConnectionState is ConnectionState.Available -> {
                            viewModel.loadWeather()
                        }
                        state.isLoading -> {}
                        else -> {
                            viewModel.loadWeather()
                        }
                    }
                }
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Icon(
                        painter = when {
                            state.isDownloadAvailable
                                    && internetConnectionState is ConnectionState.Available -> painterResource(
                                Res.drawable.ic_baseline_download_24
                            )
                            else -> painterResource(Res.drawable.ic_baseline_refresh_24)
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        NewCatchWeatherPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            weatherDescription = state.primary,
            weatherIconId = state.icon,
            onDescriptionChange = { viewModel.setWeatherPrimary(it) },
            onIconChange = { viewModel.setWeatherIconId(it) },
            onError = { primaryWeatherError = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            NewCatchTemperatureView(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                temperature = state.temperature,
                onTemperatureChange = { viewModel.setWeatherTemperature(it) },
                onError = { temperatureError = it }
            )

            NewCatchPressureView(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                pressure = state.pressure,
                onPressureChange = { viewModel.setWeatherPressure(it) },
                onError = { pressureError = it }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            NewCatchWindView(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                wind = state.windSpeed,
                windDeg = state.windDeg,
                onWindChange = { viewModel.setWeatherWindSpeed(it) },
                onWindDirChange = { viewModel.setWeatherWindDeg(it.toInt()) },
                onError = { windError = it }
            )

            NewCatchMoonView(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                moonPhase = state.moonPhase
            )
        }
    }
}


