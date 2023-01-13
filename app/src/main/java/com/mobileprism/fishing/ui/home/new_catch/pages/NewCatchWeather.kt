package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.home.new_catch.*
import com.mobileprism.fishing.ui.home.new_catch.weather.NewCatchMoonView
import com.mobileprism.fishing.ui.home.new_catch.weather.NewCatchPressureView
import com.mobileprism.fishing.ui.home.new_catch.weather.NewCatchWindView
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow

@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val weatherState by viewModel.catchWeatherState.collectAsState()

    val context = LocalContext.current
    val internetConnectionState = context.observeConnectivityAsFlow()
        .collectAsState(initial = ConnectionState.Available)

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
                    weatherState.isDownloadAvailable
                            && internetConnectionState.value is ConnectionState.Available -> {
                        viewModel.loadWeather()
                    }
                    weatherState.isLoading -> {}
                    else -> {
                        viewModel.loadWeather()
                    }
                }
            }
        ) {
            if (weatherState.isLoading) {
                CircularProgressIndicator()
            } else {
                Icon(
                    painter = when {
                        weatherState.isDownloadAvailable
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
            fishingWeather = weatherState.weather,
            onWeatherChange = viewModel::setWeather,
        )

        NewCatchTemperatureView(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(description.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            temperature = weatherState.temperature,
            onTemperatureChange = viewModel::setWeatherTemperature,
        )

        NewCatchPressureView(
            modifier = Modifier.constrainAs(press) {
                top.linkTo(temp.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            pressure = weatherState.pressure,
            onPressureChange = viewModel::setWeatherPressure,
        )

        NewCatchWindView(
            modifier = Modifier.constrainAs(wind) {
                top.linkTo(press.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            wind = weatherState.windSpeed,
            windDeg = weatherState.windDeg,
            onWindChange = viewModel::setWeatherWindSpeed,
            onWindDirChange = { viewModel.setWeatherWindDeg(it.toInt()) },
        )

        NewCatchMoonView(
            modifier = Modifier.constrainAs(moon) {
                top.linkTo(wind.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            moonPhase = weatherState.moonPhase
        )
    }
}


