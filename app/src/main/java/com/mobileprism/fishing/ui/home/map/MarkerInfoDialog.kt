package com.mobileprism.fishing.ui.home.map

import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.PrimaryText
import com.mobileprism.fishing.ui.home.views.SubtitleText
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.resources
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.viewmodels.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MarkerInfoDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    onMarkerIconClicked: (UserMapMarker) -> Unit,
    onBottomSheetClose: () -> Unit,
) {
    val context = LocalContext.current

    val viewModel: MapViewModel = getViewModel()
    val receivedMarker by viewModel.currentMarker.collectAsState()
    val weatherPreferences: WeatherPreferences = get()

    val windUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    val address by viewModel.currentMarkerAddress.collectAsState()
    val rawDistance by viewModel.currentMarkerRawDistance.collectAsState()
    val distance: String? by remember { mutableStateOf(rawDistance?.let { context.convertDistance(it) }) }
    val fishActivity: Int? by remember { viewModel.fishActivity }
    val currentWeather: CurrentWeatherFree? by remember { viewModel.currentWeather }

    receivedMarker?.let { notNullMarker ->
        LaunchedEffect(receivedMarker, viewModel.lastKnownLocation.value) {
            viewModel.setNewMarkerInfo(notNullMarker.latitude, notNullMarker.longitude)
        }
    }

    val paddingDp = 8.dp
    val cornersDp = 16.dp
    val elevationDp = 6.dp

    receivedMarker?.let { marker ->
        Card(
            shape = RoundedCornerShape(cornersDp),
            elevation = elevationDp,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier
                .zIndex(1.0f)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(paddingDp),
            onClick = { onMarkerClicked(marker, navController) }
        ) {
            AnimatedVisibility(
                true,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                    /*.noRippleClickable(
                        onClick = onDescriptionClick,
                        enabled = scaffoldState.bottomSheetState.isCollapsed
                    )*/
                ) {
                    val (locationIcon, title, area, distanceTo,
                        fish, divider, weather) = createRefs()

                    val horizontalLine = createGuidelineFromAbsoluteLeft(0.5f)
                    val verticalFabLine = createGuidelineFromAbsoluteRight(60.dp)

                    Box(modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp)
                        .constrainAs(locationIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            top.linkTo(parent.top)
                        }) {
                        IconButton(onClick = { onMarkerIconClicked(marker) }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                                contentDescription = stringResource(id = R.string.marker_icon),
                                tint = Color(marker.markerColor)
                            )
                        }

                    }

                    PrimaryText(
                        modifier = Modifier
                            .constrainAs(title) {
                                top.linkTo(locationIcon.top)
                                linkTo(locationIcon.end, verticalFabLine, 0.dp, 0.dp, 0f)
                                bottom.linkTo(locationIcon.bottom)
                                width = Dimension.fillToConstraints
                            },
                        text = when {
                            marker.title.isNotEmpty() -> marker.title
                            else -> stringResource(R.string.no_name_place)
                        } + "",
                        maxLines = 2,
                    )

                    // TODO: Баг с наездом текста на дистанцию
                    //Area name
                    SubtitleText(
                        modifier = Modifier
                            .constrainAs(area) {
                                top.linkTo(title.bottom, 4.dp)
                                linkTo(title.start, title.end, 0.dp, 32.dp, 0f)

                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        text = address ?: "",
                        maxLines = 1
                    )

                    //Distance
                    Row(
                        modifier = Modifier
                            .constrainAs(distanceTo) {
                                top.linkTo(area.top)
                                bottom.linkTo(area.bottom)
                                linkTo(area.absoluteRight, parent.absoluteRight, 8.dp, 16.dp, 1f)

                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SubtitleText(
                            text = distance ?: "",
                            textAlign = TextAlign.Center,
                        )
                    }

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
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fish),
                            contentDescription = stringResource(id = R.string.fish_desc),
                            modifier = Modifier
                                .size(45.dp)
                                .padding(6.dp),
                            tint = if (fishActivity == null) Color.LightGray else MaterialTheme.colors.primary
                        )
                        SubtitleText(
                            text = if (fishActivity != null) fishActivity.toString() + "%" else "",
                        )
                    }

                    //Divider
                    Divider(
                        modifier = Modifier
                            .constrainAs(divider) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(horizontalLine, horizontalLine, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }
                            .height(20.dp)
                            .width(1.dp),
                        color = Color.Gray,
                    )

                    //Weather
                    Row(
                        modifier = Modifier
                            .constrainAs(weather) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(horizontalLine, parent.absoluteRight, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }
                            .animateContentSize(
                                animationSpec =
                                tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                            ),
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onWeatherIconClicked(marker, navController) }) {
                            Icon(
                                painterResource(R.drawable.ic_baseline_navigation_24), "",
                                modifier = Modifier.rotate(viewModel.windIconRotation),
                                tint = if (currentWeather == null) Color.LightGray else MaterialTheme.colors.primaryVariant
                            )
                        }

                        currentWeather?.let {
                            SubtitleText(
                                text = windUnit.getWindSpeed(it.wind_speed) + " " +
                                        stringResource(windUnit.stringRes)
                            )
                        }
                    }
                }
            }
        }
    } ?: onBottomSheetClose()
}

fun onMarkerClicked(marker: UserMapMarker, navController: NavController) {
    if (marker.id != Constants.CURRENT_PLACE_ITEM_ID) {
        navController.navigate(
            MainDestinations.PLACE_ROUTE,
            Arguments.PLACE to marker
        )
    } else {
        // TODO: Нельзя перейти на экран места
    }

}

fun onWeatherIconClicked(marker: UserMapMarker, navController: NavController) {
    navController.navigate(
        "${MainDestinations.HOME_ROUTE}/${MainDestinations.WEATHER_ROUTE}",
        Arguments.PLACE to marker
    )
}





