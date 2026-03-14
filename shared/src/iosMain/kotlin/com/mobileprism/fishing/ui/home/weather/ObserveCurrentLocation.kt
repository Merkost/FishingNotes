package com.mobileprism.fishing.ui.home.weather

import androidx.compose.runtime.Composable

@Composable
actual fun ObserveCurrentLocation(
    locationPermissionGranted: Boolean,
    currentLocationTitle: String,
    onLocationReceived: (latitude: Double, longitude: Double, title: String) -> Unit
) { }
