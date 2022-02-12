package com.mobileprism.fishing.compose.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.home.new_catch.*
import com.mobileprism.fishing.compose.ui.home.views.DefaultIconButton
import com.mobileprism.fishing.compose.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

@ExperimentalComposeUiApi
@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val (subtitle, description, temp, press, wind, moon, refreshButton) = createRefs()

        val guideline = createGuidelineFromAbsoluteLeft(0.5f)

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

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.weather_cloudy,
            text = stringResource(R.string.weather)
        )

        DefaultIconButton(
            modifier = Modifier.constrainAs(refreshButton) {
                absoluteRight.linkTo(parent.absoluteRight)
                top.linkTo(subtitle.top)
                bottom.linkTo(subtitle.bottom)
            },
            icon = painterResource(id = R.drawable.ic_baseline_refresh_24),
            tint = MaterialTheme.colors.primaryVariant,
            onClick = { viewModel.refreshWeatherState() }
        )

        NewCatchWeatherPrimary(
            modifier = Modifier.constrainAs(description) {
                top.linkTo(subtitle.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            weatherDescription = viewModel.weatherPrimary.collectAsState(),
            weatherIconId = viewModel.weatherIconId.collectAsState(),
            onDescriptionChange = { viewModel.setWeatherPrimary(it) },
            onIconChange = { viewModel.setWeatherIconId(it) },
            onError = { primaryWeatherError = it }
        )

        NewCatchTemperatureView(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(description.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            temperature = viewModel.weatherTemperature.collectAsState(),
            onTemperatureChange = { viewModel.setWeatherTemperature(it) },
            onError = { temperatureError = it }
        )

        NewCatchPressureView(
            modifier = Modifier.constrainAs(press) {
                top.linkTo(temp.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            pressure = viewModel.weatherPressure.collectAsState(),
            onPressureChange = { viewModel.setWeatherPressure(it) },
            onError = { pressureError = it }
        )

        NewCatchWindView(
            modifier = Modifier.constrainAs(wind) {
                top.linkTo(press.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            wind = viewModel.weatherWindSpeed.collectAsState(),
            windDeg = viewModel.weatherWindDeg.collectAsState(),
            onWindChange = { viewModel.setWeatherWindSpeed(it) },
            onError = { windError = it }
        )

        NewCatchMoonView(
            modifier = Modifier.constrainAs(moon) {
                top.linkTo(wind.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            moonPhase = viewModel.weatherMoonPhase.collectAsState()
        )
    }
}
