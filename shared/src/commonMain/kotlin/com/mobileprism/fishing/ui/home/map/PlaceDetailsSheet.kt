package com.mobileprism.fishing.ui.home.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.weather.stringRes
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.viewmodels.MapViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.km
import fishing.shared.generated.resources.m
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PlaceDetailsCard(
    viewModel: MapViewModel,
    navController: NavController,
    onAddCatch: (UserMapMarker) -> Unit = {},
    onSaveCurrentPlace: () -> Unit = {},
) {
    val receivedMarker by viewModel.currentMarker.collectAsState()
    val windRotation by viewModel.windIconRotation.collectAsStateWithLifecycle()
    val weatherPreferences: WeatherPreferences = koinInject()
    val windUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)
    var address by remember { mutableStateOf("") }
    val addressState by viewModel.currentMarkerAddressState.collectAsState()
    val rawDistance by viewModel.currentMarkerRawDistance.collectAsState()
    val lastKnownLocation by viewModel.lastKnownLocation.collectAsState()
    val fishActivity by viewModel.fishActivity.collectAsStateWithLifecycle()
    val currentWeather by viewModel.currentWeather.collectAsStateWithLifecycle()
    val statsLoading by viewModel.placeStatsLoading.collectAsStateWithLifecycle()

    val mLabel = stringResource(Res.string.m)
    val kmLabel = stringResource(Res.string.km)
    val distance: String? by remember(rawDistance) {
        mutableStateOf(rawDistance?.let { convertDistance(it, mLabel, kmLabel) })
    }

    SetPlaceNameResultListener(addressState) { newPlaceName ->
        address = newPlaceName
    }

    receivedMarker?.let { notNullMarker ->
        LaunchedEffect(receivedMarker, lastKnownLocation) {
            viewModel.setNewMarkerInfo(notNullMarker.latitude, notNullMarker.longitude)
        }
    }

    val subtitleLoading = address.isBlank() && distance == null
    receivedMarker?.let { marker ->
        val windSpeedText = currentWeather?.let {
            "${windUnit.getWindSpeed(it.wind_speed)} ${stringResource(windUnit.stringRes)}"
        }
        val weatherIconName = currentWeather?.cloud_pct?.let { pct ->
            when {
                pct <= 20 -> "01d"
                pct <= 50 -> "02d"
                pct <= 80 -> "03d"
                else -> "04d"
            }
        }
        PlaceDetailsCardContent(
            title = marker.title,
            address = address.takeIf { it.isNotBlank() },
            distance = distance,
            subtitleLoading = subtitleLoading,
            temperatureCelsius = currentWeather?.temp?.toInt(),
            weatherIconName = weatherIconName,
            fishActivityPercent = fishActivity,
            windSpeedText = windSpeedText,
            windRotationDeg = windRotation,
            catchesCount = marker.catchesCount,
            showAddCatch = marker.id != Constants.CURRENT_PLACE_ITEM_ID,
            onCardClick = {
                if (marker.id != Constants.CURRENT_PLACE_ITEM_ID) {
                    navController.navigate(MainDestinations.Place(marker))
                } else {
                    onSaveCurrentPlace()
                }
            },
            onAddCatchClick = { onAddCatch(marker) },
        )
    }
}
