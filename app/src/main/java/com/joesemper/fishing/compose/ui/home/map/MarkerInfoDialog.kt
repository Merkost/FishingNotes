package com.joesemper.fishing.compose.ui.home.map

import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.place.UserPlaceScreen
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SubtitleText
import com.joesemper.fishing.compose.ui.resources
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.compose.ui.utils.currentFraction
import com.joesemper.fishing.compose.ui.utils.noRippleClickable
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.text.DecimalFormat


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MarkerInfoDialog(
    marker: UserMapMarker?,
    lastKnownLocation: MutableState<LatLng?>,
    mapUiState: MapUiState,
    modifier: Modifier = Modifier,
    navController: NavController,
    scaffoldState: BottomSheetScaffoldState,
    upPress: (UserMapMarker) -> Unit,
    onDescriptionClick: () -> Unit,
) {
    val context = LocalContext.current

    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val geocoder = Geocoder(context, resources().configuration.locale)

    /*val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)

    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)*/

    var address: String? by remember { mutableStateOf(null) }
    var distance: String? by remember { mutableStateOf(null) }
    val fishActivity: Int? by remember { viewModel.fishActivity }

    /*val weather = marker?.let {
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
    }*/

    marker?.let {
        LaunchedEffect(marker) {
            coroutineScope.launch(Dispatchers.Default) {
                address = null
                delay(800)
                try {
                    val position = geocoder.getFromLocation(marker.latitude, marker.longitude, 1)
                    position?.first()?.apply {
                        address = if (!subAdminArea.isNullOrBlank()) {
                            subAdminArea.replaceFirstChar { it.uppercase() }
                        } else if (!adminArea.isNullOrBlank()) {
                            adminArea.replaceFirstChar { it.uppercase() }
                        } else "Не удалось определить название"
                    }
                } catch (e: Throwable) {
                    address = "Нет соединения с сервером"
                }
            }
        }

        LaunchedEffect(marker) {
            viewModel.fishActivity.value = null
            viewModel.getFishActivity(marker.latitude, marker.longitude)
        }

        LaunchedEffect(marker) {
            viewModel.currentWeather.value = null
            viewModel.getCurrentWeather(marker.latitude, marker.longitude)
        }

        LaunchedEffect(marker, viewModel.lastKnownLocation.value) {
            coroutineScope.launch(Dispatchers.Default) {
                distance = null
                lastKnownLocation.value?.let {
                    distance = convertDistance(
                        SphericalUtil.computeDistanceBetween(
                            com.google.android.gms.maps.model.LatLng(
                                marker.latitude,
                                marker.longitude
                            ),
                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                        )
                    )
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

            AnimatedVisibility(
                scaffoldState.currentFraction == 0f,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp)
                        .noRippleClickable(
                            onClick = onDescriptionClick,
                            enabled = scaffoldState.bottomSheetState.isCollapsed
                        )
                ) {
                    val (locationIcon, title, area, distanceTo, fish, time8, time16, pressNowVal, press8Val,
                        press16Val, press8Icon, press16Icon, loading, noNetwork) = createRefs()

                    val horizontalLine = createGuidelineFromAbsoluteLeft(0.5f)


                    Box(modifier = Modifier
                        .size(64.dp).padding(16.dp)

                        .constrainAs(locationIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            top.linkTo(parent.top)
                        }) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                            contentDescription = "Marker",
                            tint = Color(marker.markerColor)
                        )
                    }

                    PrimaryText(
                        modifier = Modifier
                            .constrainAs(title) {
                                top.linkTo(locationIcon.top)
                                linkTo(locationIcon.end, parent.end, 0.dp, 32.dp, 0f)
                                bottom.linkTo(locationIcon.bottom)
                            },
                        text = when {
                            marker.title.isNotEmpty() -> marker.title
                            else -> stringResource(R.string.no_name_place)
                        } + "",
                    )

                    //Area name
                    SubtitleText(
                        modifier = Modifier
                            .constrainAs(area) {
                                top.linkTo(title.bottom, 4.dp)
                                linkTo(locationIcon.end, parent.end, 0.dp, 80.dp, 0f)
                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        text = address ?: "",
                        textColor = if (address == null) Color.LightGray else secondaryTextColor
                    )

                    //Distance
                    SubtitleText(
                        modifier = Modifier
                            .constrainAs(distanceTo) {
                                top.linkTo(area.top)
                                bottom.linkTo(area.bottom)
                                linkTo(area.absoluteRight, parent.absoluteRight, 0.dp, 0.dp, 1f)
                            }.width(80.dp),

                        /*overflow = TextOverflow.Ellipsis,*/
                        text = distance ?: "",
                        textColor = if (distance == null) Color.LightGray else secondaryTextColor,
                        textAlign = TextAlign.Center,
                    )

                    //Fish activity
                    Row(
                        modifier = Modifier
                            .constrainAs(fish) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(parent.absoluteLeft, horizontalLine, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fish),
                            contentDescription = "Marker",
                            modifier = Modifier.size(45.dp).padding(6.dp),
                            tint = if (fishActivity == null) Color.LightGray else MaterialTheme.colors.primary
                        )
                        SubtitleText(
                            text = if (fishActivity != null) fishActivity.toString() + "%" else "",
                            textColor = if (fishActivity == null) Color.LightGray else secondaryTextColor
                        )
                    }

                    //Weather



                    /* //weatherForecast
                    if (connectionState is ConnectionState.Available) {
                        weather?.value?.let { forecast ->

                            val guideline = createGuidelineFromAbsoluteLeft(0.5f)

                            SecondaryTextSmall(
                                modifier = Modifier.constrainAs(timeNow) {
                                    top.linkTo(area.bottom, 16.dp)
                                    absoluteLeft.linkTo(parent.absoluteLeft)
                                    absoluteRight.linkTo(guideline, 16.dp)
                                },
                                text = stringResource(id = R.string.now)
                            )

                            SecondaryTextSmall(
                                modifier = Modifier.constrainAs(time8) {
                                    top.linkTo(area.bottom, 16.dp)
                                    absoluteLeft.linkTo(guideline)
                                    absoluteRight.linkTo(guideline)
                                },
                                text = stringResource(R.string.in_8h)
                            )

                            SecondaryTextSmall(
                                modifier = Modifier.constrainAs(time16) {
                                    top.linkTo(area.bottom, 16.dp)
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
                                text = pressureUnit.getPressure(
                                    forecast.hourly.first().pressure) + " " + pressureUnit.name
                            )

                            PrimaryTextSmall(
                                modifier = Modifier.constrainAs(press8Val) {
                                    top.linkTo(time8.bottom, 4.dp)
                                    absoluteLeft.linkTo(time8.absoluteLeft)
                                    absoluteRight.linkTo(time8.absoluteRight)
                                },
                                text = pressureUnit.getPressure(
                                    forecast.hourly[7].pressure) + " " + pressureUnit.name
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
                                text = pressureUnit.getPressure(
                                    forecast.hourly[15].pressure) + " " + pressureUnit.name
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
                                    top.linkTo(area.bottom)
                                    bottom.linkTo(parent.bottom)
                                    absoluteRight.linkTo(parent.absoluteRight)
                                    absoluteLeft.linkTo(parent.absoluteLeft)
                                }
                            )
                        }
                    } else {
                        SecondaryTextSmall(
                            modifier = Modifier.constrainAs(noNetwork) {
                                top.linkTo(area.bottom)
                                bottom.linkTo(parent.bottom)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(parent.absoluteRight)
                            },
                            text = stringResource(R.string.no_internet_connection)
                        )
                    }*/
                }
            }
            AnimatedVisibility(scaffoldState.currentFraction != 0f) {
                UserPlaceScreen({ upPress(marker) }, navController, place = marker)
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


fun convertDistance(distanceInMeters: Double): String {
    val df = DecimalFormat("#.#")

    return when (distanceInMeters.toInt()) {
        in 0..999 -> distanceInMeters.toInt().toString() + " m"
        in 1000..9999 -> df.format(distanceInMeters / 1000f).toString() + " km"
        else -> distanceInMeters.div(1000).toInt().toString() + " km"
    }
}



