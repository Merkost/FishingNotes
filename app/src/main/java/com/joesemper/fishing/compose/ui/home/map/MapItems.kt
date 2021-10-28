package com.joesemper.fishing.compose.ui.home.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import kotlinx.coroutines.CoroutineScope

@ExperimentalPermissionsApi
@Composable
fun MyLocationButton(
    coroutineScope: CoroutineScope,
    mapView: MapView,
    lastKnownLocation: LatLng,
    modifier: Modifier,
) {
    Card(shape = CircleShape, modifier = modifier) {
        androidx.compose.material.IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { lastKnownLocation?.let {
                moveCameraToLocation(coroutineScope, mapView, lastKnownLocation)
            } }) {
            Icon(Icons.Default.MyLocation, stringResource(R.string.my_location))
        }
    }
}

@Composable
fun MapLayersButton(layersSelectionMode: MutableState<Boolean>, modifier: Modifier) {
    Card(shape = CircleShape, modifier = modifier) {
        IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { layersSelectionMode.value = true }) {
            Icon(painterResource(R.drawable.ic_baseline_layers_24), stringResource(R.string.layers))
        }
    }
}

@Composable
fun LayersView(
    mapView: MapView,
    mapLayersSelection: MutableState<Boolean>,
    mapType: MutableState<Int>
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(250.dp)
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.map_type))
                Card(shape = CircleShape, modifier = Modifier.size(20.dp)) {
                    IconButton(onClick = { mapLayersSelection.value = false }) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MapLayerItem(
                    mapType,
                    layer = MapTypes.roadmap,
                    painter = painterResource(R.drawable.ic_map_default),
                    name = stringResource(R.string.roadmap)
                )
                MapLayerItem(
                    mapType,
                    layer = MapTypes.hybrid,
                    painter = painterResource(R.drawable.ic_map_satellite),
                    name = stringResource(R.string.satellite)
                )
                MapLayerItem(
                    mapType,
                    layer = MapTypes.terrain,
                    painter = painterResource(R.drawable.ic_map_terrain),
                    name = stringResource(R.string.terrain)
                )

                LaunchedEffect(mapType.value) {
                    val googleMap = mapView.awaitMap()
                    googleMap.mapType = mapType.value
                }
            }
        }
    }
}