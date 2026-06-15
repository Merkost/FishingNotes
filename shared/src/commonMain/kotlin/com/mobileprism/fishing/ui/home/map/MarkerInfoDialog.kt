package com.mobileprism.fishing.ui.home.map

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.HomeTabs
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.ui.home.weather.stringRes
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.viewmodels.MapViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerInfoDialog(
    viewModel: MapViewModel,
    navController: NavController,
    onMarkerIconClicked: (UserMapMarker) -> Unit,
    onAddCatch: (UserMapMarker) -> Unit = {},
    onSaveCurrentPlace: () -> Unit = {},
) {
    val windRotation by viewModel.windIconRotation.collectAsStateWithLifecycle()
    val receivedMarker by viewModel.currentMarker.collectAsState()
    val weatherPreferences: WeatherPreferences = koinInject()

    val windUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)
    var address by remember { mutableStateOf("") }
    val addressState by viewModel.currentMarkerAddressState.collectAsState()

    SetPlaceNameResultListener(addressState) { newPlaceName ->
        address = newPlaceName
    }

    val rawDistance by viewModel.currentMarkerRawDistance.collectAsState()
    val lastKnownLocation by viewModel.lastKnownLocation.collectAsState()

    val mLabel = stringResource(Res.string.m)
    val kmLabel = stringResource(Res.string.km)
    val distance: String? by remember(rawDistance) {
        mutableStateOf(rawDistance?.let { convertDistance(it, mLabel, kmLabel) })
    }

    val fishActivity by viewModel.fishActivity.collectAsStateWithLifecycle()
    val currentWeather: CurrentWeatherFree? by viewModel.currentWeather.collectAsStateWithLifecycle()


    receivedMarker?.let { notNullMarker ->
        LaunchedEffect(receivedMarker, lastKnownLocation) {
            viewModel.setNewMarkerInfo(notNullMarker.latitude, notNullMarker.longitude)
        }
    }

    val paddingDp = 8.dp
    val cornersDp = 16.dp
    val elevationDp = 6.dp

    receivedMarker?.let { marker ->
        Card(
            shape = RoundedCornerShape(cornersDp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevationDp),
            colors = CardDefaults.cardColors(containerColor = FishingTheme.colorScheme.surface),
            modifier = Modifier
                .zIndex(1.0f)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(paddingDp),
            onClick = { onMarkerClicked(marker, navController, onSaveCurrentPlace) }
        ) {
            AnimatedVisibility(
                true,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    // Top row: location icon, title, add catch button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier
                            .size(64.dp)
                            .padding(16.dp)
                        ) {
                            IconButton(onClick = { onMarkerIconClicked(marker) }) {
                                Icon(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                                    contentDescription = stringResource(Res.string.marker_icon),
                                    tint = Color(marker.markerColor)
                                )
                            }
                        }

                        AppText(
                            modifier = Modifier.weight(1f),
                            text = when {
                                marker.title.isNotEmpty() -> marker.title
                                else -> stringResource(Res.string.no_name_place)
                            } + "",
                            style = AppTextStyle.Title,
                            maxLines = 2,
                        )

                        if (marker.id != Constants.CURRENT_PLACE_ITEM_ID) {
                            IconButton(
                                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                                onClick = { onAddCatch(marker) }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_add_catch),
                                    contentDescription = stringResource(Res.string.add_new_catch),
                                    tint = FishingTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Area name + distance row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 64.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = LinearOutSlowInEasing
                                    )
                                ),
                            text = address,
                            style = AppTextStyle.Subtitle,
                            color = FishingTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )

                        AppText(
                            modifier = Modifier
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                                .padding(start = 8.dp),
                            text = distance ?: "",
                            style = AppTextStyle.Subtitle,
                            color = FishingTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom row: fish activity | divider | weather
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Fish activity
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = LinearOutSlowInEasing
                                    )
                                ),
                            horizontalArrangement = Arrangement.spacedBy(
                                Spacing.sm,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.fish),
                                contentDescription = stringResource(Res.string.fish_desc),
                                modifier = Modifier
                                    .size(45.dp)
                                    .padding(Spacing.sm),
                                tint = if (fishActivity == null) FishingTheme.colorScheme.outlineVariant else FishingTheme.colorScheme.primary
                            )
                            AppText(
                                text = if (fishActivity != null) fishActivity.toString() + "%" else "",
                                style = AppTextStyle.Subtitle,
                                color = FishingTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Divider
                        HorizontalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp),
                            color = FishingTheme.colorScheme.outline,
                        )

                        // Weather
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(
                                    animationSpec =
                                    tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                                ),
                            horizontalArrangement = Arrangement.spacedBy(
                                Spacing.sm,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { onWeatherIconClicked(marker, navController) }) {
                                Icon(
                                    painterResource(Res.drawable.ic_baseline_navigation_24), "",
                                    modifier = Modifier.rotate(windRotation),
                                    tint = if (currentWeather == null) FishingTheme.colorScheme.outlineVariant else FishingTheme.colorScheme.tertiary
                                )
                            }

                            currentWeather?.let {
                                AppText(
                                    text = windUnit.getWindSpeed(it.wind_speed) + " " +
                                            stringResource(windUnit.stringRes),
                                    style = AppTextStyle.Subtitle,
                                    color = FishingTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun onMarkerClicked(marker: UserMapMarker, navController: NavController, onSaveCurrentPlace: () -> Unit) {
    if (marker.id != Constants.CURRENT_PLACE_ITEM_ID) {
        navController.navigate(MainDestinations.Place(marker))
    } else {
        onSaveCurrentPlace()
    }
}

fun onWeatherIconClicked(marker: UserMapMarker, navController: NavController) {
    navController.navigate(
        HomeTabs.WeatherTab(place = marker)
    )
}





