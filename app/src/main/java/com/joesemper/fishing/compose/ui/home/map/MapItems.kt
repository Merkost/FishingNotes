package com.joesemper.fishing.compose.ui.home.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import kotlinx.coroutines.CoroutineScope

@Composable
fun MyLocationButton(
    coroutineScope: CoroutineScope,
    mapView: MapView,
    lastKnownLocation: LatLng,
    modifier: Modifier
) {
    Card(shape = CircleShape, modifier = modifier) {
        androidx.compose.material.IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { moveCameraToLocation(coroutineScope, mapView, lastKnownLocation) }) {
            Icon(Icons.Default.MyLocation, stringResource(R.string.my_location))
        }
    }
}

@Composable
fun MapLayersButton(layersSelectionMode: MutableState<Boolean>, modifier: Modifier) {
    Card(shape = CircleShape, modifier = modifier) {
        androidx.compose.material.IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { layersSelectionMode.value = true }) {
            Icon(Icons.Default.Apps, stringResource(R.string.layers))
        }
    }
}