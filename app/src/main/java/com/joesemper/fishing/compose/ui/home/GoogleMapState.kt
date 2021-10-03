package com.joesemper.fishing.compose.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.google.android.libraries.maps.MapView

@ExperimentalMaterialApi
class GoogleMapState(
    val mapView: MapView,
)

/**
 * Create and [remember] a [GoogleMapState].
 *
 * @param drawerState The state of the navigation drawer.
 * @param bottomSheetState The state of the persistent bottom sheet.
 * @param snackbarHostState The [SnackbarHostState] used to show snackbars inside the scaffold.
 */
@Composable
@ExperimentalMaterialApi
fun rememberGoogleMapState(
    //drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    //bottomSheetState: BottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed),
    mapViewState: MapView = rememberMapViewWithLifecycle()
): GoogleMapState {
    return remember(mapViewState) {
        GoogleMapState(
            mapView = mapViewState
        )
    }
}

