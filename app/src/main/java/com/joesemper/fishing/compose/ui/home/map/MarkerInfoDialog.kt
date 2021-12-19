package com.joesemper.fishing.compose.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.home.weather.PressureValues
import com.joesemper.fishing.compose.ui.home.weather.getPressure
import com.joesemper.fishing.compose.ui.utils.currentFraction
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.utils.network.ConnectionState
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@ExperimentalMaterialApi
@Composable
fun MarkerInfoDialog(
    marker: UserMapMarker?,
    mapState: MapUiState,
    scaffoldState: BottomSheetScaffoldState,
    onDescriptionClick: (UserMapMarker) -> Unit,
    ) {
    val context = LocalContext.current
    val viewModel: MapViewModel = getViewModel()

    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg.name)

    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    val weather = marker?.let {
        if (connectionState is ConnectionState.Available) {
            val result by viewModel.getWeather(it.latitude, it.longitude)
                .collectAsState(RetrofitWrapper.Success<WeatherForecast?>(null))

            when (result) {
                is RetrofitWrapper.Success<*> -> {
                    return@let mutableStateOf((result as RetrofitWrapper.Success<WeatherForecast?>).data)
                }
                else -> return@let null
            }
        } else {
            null
        }
    }


    val paddingDp = animateDpAsState(
        when (scaffoldState.currentFraction) {
            0.0f -> 8.dp
            else -> 0.dp
        }
    )


    val elevationDp = animateDpAsState(
        when (mapState) {
            is MapUiState.BottomSheetInfoMode -> (12.dp)
            is MapUiState.BottomSheetFullyExpanded -> (0.dp)
            else -> (12.dp)
        }
    )

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(paddingDp.value),
    ) {
        Spacer(modifier = Modifier.size(1.dp))
        marker?.let {

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    /*.clickable {
                        //onDescriptionClick(it)
                    }*/
            ) {
                val (locationIcon, title, timeNow, time8, time16, pressNowVal, press8Val,
                    press16Val, press8Icon, press16Icon, loading, noNetwork) = createRefs()

                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .constrainAs(locationIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                            top.linkTo(title.top)
                            bottom.linkTo(title.bottom)
                        },
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = "Marker",
                    tint = Color(marker.markerColor)
                )

                PrimaryText(
                    modifier = Modifier
                        .constrainAs(title) {
                            top.linkTo(parent.top, 16.dp)
                            bottom.linkTo(parent.bottom, 100.dp)
                            linkTo(locationIcon.end, parent.end, 8.dp, 8.dp, 0f)
                        },
                    text = marker.title,
                )

                if (connectionState is ConnectionState.Available) {
                    weather?.value?.let { forecast ->

                        val guideline = createGuidelineFromAbsoluteLeft(0.5f)

                        SecondaryTextSmall(
                            modifier = Modifier.constrainAs(timeNow) {
                                top.linkTo(title.bottom, 16.dp)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(guideline, 16.dp)
                            },
                            text = stringResource(id = R.string.now)
                        )

                        SecondaryTextSmall(
                            modifier = Modifier.constrainAs(time8) {
                                top.linkTo(title.bottom, 16.dp)
                                absoluteLeft.linkTo(guideline)
                                absoluteRight.linkTo(guideline)
                            },
                            text = stringResource(R.string.in_8h)
                        )

                        SecondaryTextSmall(
                            modifier = Modifier.constrainAs(time16) {
                                top.linkTo(title.bottom, 16.dp)
                                absoluteLeft.linkTo(guideline, 16.dp)
                                absoluteRight.linkTo(parent.absoluteRight)
                            },
                            text = stringResource(R.string.in_16h)
                        )

                        PrimaryTextSmall(
                            modifier = Modifier.constrainAs(pressNowVal) {
                                top.linkTo(timeNow.bottom, 4.dp)
                                absoluteLeft.linkTo(timeNow.absoluteLeft)
                                absoluteRight.linkTo(timeNow.absoluteRight)
                            },
                            text = getPressure(
                                forecast.hourly.first().pressure,
                                PressureValues.valueOf(pressureUnit)
                            ) + " " + pressureUnit
                        )

                        PrimaryTextSmall(
                            modifier = Modifier.constrainAs(press8Val) {
                                top.linkTo(time8.bottom, 4.dp)
                                absoluteLeft.linkTo(time8.absoluteLeft)
                                absoluteRight.linkTo(time8.absoluteRight)
                            },
                            text = getPressure(
                                forecast.hourly[7].pressure,
                                PressureValues.valueOf(pressureUnit)
                            ) + " " + pressureUnit
                        )

                        Icon(
                            modifier = Modifier
                                .constrainAs(press8Icon) {
                                    top.linkTo(press8Val.top)
                                    bottom.linkTo(press8Val.bottom)
                                    absoluteLeft.linkTo(press8Val.absoluteRight)
                                }
                                .rotate(getIconRotationByWeatherIn8H(forecast)),
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_up_24),
                            contentDescription = stringResource(id = R.string.pressure),
                            tint = getIconTintByWeatherIn8H(forecast)
                        )

                        PrimaryTextSmall(
                            modifier = Modifier.constrainAs(press16Val) {
                                top.linkTo(time16.bottom, 4.dp)
                                absoluteLeft.linkTo(time16.absoluteLeft)
                                absoluteRight.linkTo(time16.absoluteRight)
                            },
                            text = getPressure(
                                forecast.hourly[15].pressure,
                                PressureValues.valueOf(pressureUnit)
                            ) + " " + pressureUnit
                        )

                        Icon(
                            modifier = Modifier
                                .constrainAs(press16Icon) {
                                    top.linkTo(press16Val.top)
                                    bottom.linkTo(press16Val.bottom)
                                    absoluteLeft.linkTo(press16Val.absoluteRight)
                                }
                                .rotate(getIconRotationByWeatherIn16H(forecast)),
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_up_24),
                            contentDescription = stringResource(id = R.string.pressure),
                            tint = getIconTintByWeatherIn16H(forecast)
                        )
                    }

                    if (weather?.value == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.constrainAs(loading) {
                                top.linkTo(title.bottom)
                                bottom.linkTo(parent.bottom)
                                absoluteRight.linkTo(parent.absoluteRight)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                            }
                        )
                    }
                } else {
                    SecondaryTextSmall(
                        modifier = Modifier.constrainAs(noNetwork) {
                            top.linkTo(title.bottom)
                            bottom.linkTo(parent.bottom)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                        },
                        text = stringResource(R.string.no_internet_connection)
                    )
                }
            }
        }
        if (paddingDp.value == 0.dp) {
            Spacer(modifier = Modifier.fillMaxSize())
        }
    }

}


