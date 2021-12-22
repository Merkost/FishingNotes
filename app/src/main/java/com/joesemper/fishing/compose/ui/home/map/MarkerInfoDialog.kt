package com.joesemper.fishing.compose.ui.home.map

import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.home.place.UserPlaceScreen
import com.joesemper.fishing.compose.ui.home.weather.PressureValues
import com.joesemper.fishing.compose.ui.home.weather.getPressure
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.compose.ui.utils.currentFraction
import com.joesemper.fishing.compose.ui.utils.noRippleClickable
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.utils.network.ConnectionState
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.vponomarenko.compose.shimmer.shimmer
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MarkerInfoDialog(
    marker: UserMapMarker?,
    mapUiState: MapUiState,
    modifier: Modifier = Modifier,
    navController: NavController,
    scaffoldState: BottomSheetScaffoldState,
    upPress: () -> Unit,
    onDescriptionClick: () -> Unit,

) {
    val context = LocalContext.current

    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val geocoder = Geocoder(context)

    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg.name)

    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    var address: String? by remember {
        mutableStateOf(null)
    }

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

    marker?.let {


        LaunchedEffect(marker) {
            address = null
            coroutineScope.launch(Dispatchers.Default) {
                delay(800)
                try {
                    val addresses = geocoder.getFromLocation(
                        marker.latitude,
                        marker.longitude,
                        1
                    )
                    address =
                        if (addresses != null && addresses.size > 0) {
                            addresses[0].getAddressLine(0)
                        } else "Не удалось получить адрес"
                    /*addresses?.first()?.let { address ->
                    viewModel.showMarker.value = true
                    if (!address.subAdminArea.isNullOrBlank()) {
                        viewModel.chosenPlace.value =
                            address.subAdminArea.replaceFirstChar { it.uppercase() }
                    } else if (!address.adminArea.isNullOrBlank()) {
                        viewModel.chosenPlace.value = address.adminArea
                            .replaceFirstChar { it.uppercase() }
                    } else viewModel.chosenPlace.value = "Место без названия"
                }*/
                } catch (e: Throwable) {
                    address = "Нет соединения с сервером"
                }
            }
        }
    }


    val paddingDp = animateDpAsState(((1f - scaffoldState.currentFraction) * 8).dp)
    val cornersDp = animateDpAsState(((1f - scaffoldState.currentFraction) * 16).dp)
    val elevationDp = animateDpAsState(((1f - scaffoldState.currentFraction) * 6).dp)

    Card(
        shape = RoundedCornerShape(cornersDp.value),
        elevation = elevationDp.value,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(paddingDp.value),
    ) {

        viewModel.currentMarker.value?.let { marker ->

            AnimatedVisibility(scaffoldState.currentFraction == 0f,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp)
                        .noRippleClickable(
                            onClick = onDescriptionClick,
                            enabled = scaffoldState.bottomSheetState.isCollapsed
                        )
                ) {
                    val (locationIcon, title, street, timeNow, time8, time16, pressNowVal, press8Val,
                        press16Val, press8Icon, press16Icon, loading, noNetwork) = createRefs()

                    Icon(
                        modifier = Modifier
                            .size(28.dp)
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
                                linkTo(locationIcon.end, parent.end, 8.dp, 8.dp, 0f)
                            },
                        text = marker.title,
                    )

                    SubtitleText(
                        modifier = Modifier
                            .constrainAs(street) {
                                top.linkTo(title.bottom, 4.dp)
                                /*bottom.linkTo(timeNow.top, 8.dp)*/
                                linkTo(title.start, parent.end, 0.dp, 0.dp, 0f)
                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        /*overflow = TextOverflow.Ellipsis,*/
                        text = address ?: "",
                        textColor = if (address == null) Color.LightGray else secondaryTextColor
                    )

                    //weatherForecast
                    if (connectionState is ConnectionState.Available) {
                        weather?.value?.let { forecast ->

                            val guideline = createGuidelineFromAbsoluteLeft(0.5f)

                            SecondaryTextSmall(
                                modifier = Modifier.constrainAs(timeNow) {
                                    top.linkTo(street.bottom, 16.dp)
                                    absoluteLeft.linkTo(parent.absoluteLeft)
                                    absoluteRight.linkTo(guideline, 16.dp)
                                },
                                text = stringResource(id = R.string.now)
                            )

                            SecondaryTextSmall(
                                modifier = Modifier.constrainAs(time8) {
                                    top.linkTo(street.bottom, 16.dp)
                                    absoluteLeft.linkTo(guideline)
                                    absoluteRight.linkTo(guideline)
                                },
                                text = stringResource(R.string.in_8h)
                            )

                            SecondaryTextSmall(
                                modifier = Modifier.constrainAs(time16) {
                                    top.linkTo(street.bottom, 16.dp)
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
                                    top.linkTo(street.bottom)
                                    bottom.linkTo(parent.bottom)
                                    absoluteRight.linkTo(parent.absoluteRight)
                                    absoluteLeft.linkTo(parent.absoluteLeft)
                                }
                            )
                        }
                    } else {
                        SecondaryTextSmall(
                            modifier = Modifier.constrainAs(noNetwork) {
                                top.linkTo(street.bottom)
                                bottom.linkTo(parent.bottom)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(parent.absoluteRight)
                            },
                            text = stringResource(R.string.no_internet_connection)
                        )
                    }
                }
            }
            AnimatedVisibility(scaffoldState.currentFraction != 0f) {
                UserPlaceScreen(upPress, navController, place = marker)
            }

        }

    }
    AnimatedVisibility(
        scaffoldState.bottomSheetState.progress.fraction != 0f,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500)),
        /*paddingDp.value == 0.dp
            && !scaffoldState.bottomSheetState.isAnimationRunning*/
        /*scaffoldState.currentFraction != 0.0f*/
    ) {
        Spacer(modifier = Modifier.fillMaxSize())
    }

}


