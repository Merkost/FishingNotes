package com.mobileprism.fishing.ui.home.weather

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mobileprism.fishing.ui.home.map.LocationState
import com.mobileprism.fishing.utils.location.LocationManager
import org.koin.compose.koinInject

@Composable
actual fun ObserveCurrentLocation(
    locationPermissionGranted: Boolean,
    currentLocationTitle: String,
    onLocationReceived: (latitude: Double, longitude: Double, title: String) -> Unit
) {
    val locationManager: LocationManager = koinInject()

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            locationManager.getCurrentLocationFlow().collect { locationState ->
                if (locationState is LocationState.LocationGranted) {
                    onLocationReceived(
                        locationState.location.latitude,
                        locationState.location.longitude,
                        currentLocationTitle
                    )
                }
            }
        }
    }
}
