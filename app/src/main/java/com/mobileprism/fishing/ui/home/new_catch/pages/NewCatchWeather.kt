package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.home.new_catch.*
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow

@ExperimentalComposeUiApi
@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.catchWeatherState.collectAsState()

    val context = LocalContext.current
    val internetConnectionState = context.observeConnectivityAsFlow()
        .collectAsState(initial = ConnectionState.Available)

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

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (subtitle, noInternet, description, temp, press, wind, moon, refreshButton) = createRefs()

        val guideline = createGuidelineFromAbsoluteLeft(0.5f)

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = R.drawable.weather_cloudy,
            text = stringResource(R.string.weather)
        )

        if (internetConnectionState.value is ConnectionState.Unavailable) {
            Icon(
                modifier = Modifier
                    .constrainAs(noInternet) {
                        top.linkTo(subtitle.top)
                        bottom.linkTo(subtitle.bottom)
                        absoluteLeft.linkTo(subtitle.absoluteRight, 8.dp)
                    },
                painter = painterResource(id = R.drawable.ic_no_internet),
                contentDescription = null,
                tint = MaterialTheme.customColors.secondaryIconColor
            )
        }

        IconButton(
            modifier = Modifier.constrainAs(refreshButton) {
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                top.linkTo(subtitle.top)
                bottom.linkTo(subtitle.bottom)
            },
            onClick = {
                when {
                    state.isDownloadAvailable
                            && internetConnectionState.value is ConnectionState.Available -> {
                        viewModel.loadWeather()
                    }
                    state.isLoading -> {}
                    else -> {
                        viewModel.refreshWeatherState()
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
                                && internetConnectionState.value is ConnectionState.Available -> painterResource(
                            id = R.drawable.ic_baseline_download_24
                        )
                        else -> painterResource(id = R.drawable.ic_baseline_refresh_24)
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colors.primaryVariant
                )
            }
        }

        NewCatchWeatherPrimary(
            modifier = Modifier.constrainAs(description) {
                top.linkTo(subtitle.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            weatherDescription = state.primary,
            weatherIconId = state.icon,
            onDescriptionChange = { viewModel.setWeatherPrimary(it) },
            onIconChange = { viewModel.setWeatherIconId(it) },
            onError = { primaryWeatherError = it }
        )

        NewCatchTemperatureView(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(description.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            temperature = state.temperature,
            onTemperatureChange = { viewModel.setWeatherTemperature(it) },
            onError = { temperatureError = it }
        )

        NewCatchPressureView(
            modifier = Modifier.constrainAs(press) {
                top.linkTo(temp.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            pressure = state.pressure,
            onPressureChange = { viewModel.setWeatherPressure(it) },
            onError = { pressureError = it }
        )

        NewCatchWindView(
            modifier = Modifier.constrainAs(wind) {
                top.linkTo(press.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            wind = state.windSpeed,
            windDeg = state.windDeg,
            onWindChange = { viewModel.setWeatherWindSpeed(it) },
            onWindDirChange = { viewModel.setWeatherWindDeg(it.toInt()) },
            onError = { windError = it }
        )

        NewCatchMoonView(
            modifier = Modifier.constrainAs(moon) {
                top.linkTo(wind.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            moonPhase = state.moonPhase
        )
    }
}


