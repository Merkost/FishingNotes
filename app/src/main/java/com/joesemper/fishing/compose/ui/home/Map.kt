package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.compose.ui.MapViewContainer
import com.joesemper.fishing.compose.ui.rememberMapViewWithLifecycle

@ExperimentalPermissionsApi
@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val mapView = rememberMapViewWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Spacer(Modifier.statusBarsHeight())
        MapViewContainer(mapView, 43.119808, 131.886917)
    }
}
//Surface(modifier = modifier.fillMaxSize(), color = Color.Blue) {
//
//}

