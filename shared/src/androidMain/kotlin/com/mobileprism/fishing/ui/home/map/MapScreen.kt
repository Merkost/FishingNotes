package com.mobileprism.fishing.ui.home.map

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.mobileprism.fishing.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.UserPreferences
import android.app.Activity
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.defaultFabBottomPadding
import com.mobileprism.fishing.viewmodels.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    addPlaceOnStart: Boolean = false,
    place: UserMapMarker?,
    upPress: () -> Unit,
) {

    val viewModel: MapViewModel = koinViewModel()
    LaunchedEffect(place) {
        viewModel.setPlace(place)
    }
    LaunchedEffect(addPlaceOnStart) {
        viewModel.setAddingPlace(addPlaceOnStart)
    }
    val context = LocalContext.current

    val mapUiState by viewModel.mapUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val analyticsTracker = LocalAnalytics.current
    val userPreferences: UserPreferences = koinInject()
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showModalBottomSheet by remember { mutableStateOf(false) }
    var newPlaceDialog by remember { mutableStateOf(false) }
    var mapLayersSelection by rememberSaveable { mutableStateOf(false) }


    BackPressHandler(
        mapUiState = mapUiState,
        navController = navController,
        onBackPressedCallback = viewModel::resetMapUiState,
        upPress = { (context as Activity).finishAffinity() },
    )

    if (showModalBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModalBottomSheet = false },
            sheetState = modalBottomSheetState,
            shape = Constants.modalBottomSheetCorners,
        ) {
            MapModalBottomSheet(mapPreferences = userPreferences)
        }
    }

    Box(modifier = modifier) {
        MapScaffold(
            mapUiState = mapUiState,
            onDismissCard = viewModel::resetMapUiState,
            fab = {
                MapFab(
                    viewModel = viewModel,
                    onClick = {
                        when (mapUiState) {
                            MapUiState.NormalMode -> viewModel.setPlaceSelectionMode()
                            MapUiState.PlaceSelectMode -> {
                                newPlaceDialog = true
                                viewModel.resetMapUiState()
                            }
                            MapUiState.BottomSheetInfoMode -> { }
                        }
                    },
                    onLongPress = { viewModel.quickAddPlace(name = context.getString(R.string.no_name_place)) },
                    userSettings = userPreferences,
                )
            },
            bottomCard = {
                MarkerInfoDialog(
                    viewModel = viewModel,
                    navController = navController,
                    onMarkerIconClicked = viewModel::onMarkerClicked,
                    onAddCatch = { marker ->
                        navController.navigate(
                            MainDestinations.NewCatch(place = marker)
                        )
                    },
                    onSaveCurrentPlace = { newPlaceDialog = true },
                )
            }
        ) {
            MapControls(
                mapUiState = mapUiState,
                viewModel = viewModel,
                userPreferences = userPreferences,
                useZoomButtons = useZoomButtons,
                mapLayersSelection = mapLayersSelection,
                onMapLayersSelectionChanged = { mapLayersSelection = it },
                onMapSettingsClicked = {
                    analyticsTracker.logEvent(AnalyticsEvent.MapSettings)
                    showModalBottomSheet = true
                },
                newPlaceDialog = newPlaceDialog,
                onNewPlaceDialogDismiss = { newPlaceDialog = false },
            )
        }
    }
}

@Composable
private fun MapControls(
    mapUiState: MapUiState,
    viewModel: MapViewModel,
    userPreferences: UserPreferences,
    useZoomButtons: Boolean,
    mapLayersSelection: Boolean,
    onMapLayersSelectionChanged: (Boolean) -> Unit,
    onMapSettingsClicked: () -> Unit,
    newPlaceDialog: Boolean,
    onNewPlaceDialogDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Map — full-size, edge-to-edge, no insets
        MapLayout(modifier = Modifier.fillMaxSize())

        // Scrim for layers selection dismissal
        if (mapLayersSelection) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .zIndex(4f)
                    .clickable { onMapLayersSelectionChanged(false) }
            ) {}
        }

        // Layer 2: Controls overlay with status bar inset padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            // Top section: left buttons, center tile, right buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.Top
            ) {
                // Left controls
                Column {
                    MapLayersButton(modifier = Modifier) { onMapLayersSelectionChanged(true) }
                    Spacer(modifier = Modifier.height(16.dp))
                    MapSettingsButton(modifier = Modifier, onCLick = onMapSettingsClicked)
                }

                // Center — PlaceTileView (only visible in place select mode)
                Spacer(modifier = Modifier.weight(1f))
                androidx.compose.animation.AnimatedVisibility(
                    visible = mapUiState == MapUiState.PlaceSelectMode && !mapLayersSelection,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                ) {
                    PlaceTileView(modifier = Modifier.wrapContentSize().padding(horizontal = 8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))

                // Right controls
                Column(horizontalAlignment = Alignment.End) {
                    MyLocationButton(
                        userPreferences = userPreferences,
                        onClick = viewModel::onMyLocationClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CompassButton(
                        mapBearing = viewModel.mapBearing.collectAsState(),
                        onClick = viewModel::resetMapBearing
                    )
                }
            }

            // Zoom buttons (center-end)
            if (useZoomButtons) {
                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                    MapZoomInButton(onClick = viewModel::onZoomInClick)
                    Spacer(modifier = Modifier.height(8.dp))
                    MapZoomOutButton(onClick = viewModel::onZoomOutClick)
                }
            }

            // Pointer icon (center of screen)
            AnimatedVisibility(
                visible = mapUiState == MapUiState.PlaceSelectMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 65.dp)
            ) {
                PointerIcon(viewModel.placeTileViewNameState.collectAsState().value.pointerState)
            }
        }

        // LayersView overlay (above scrim)
        AnimatedVisibility(
            visible = mapLayersSelection,
            enter = expandIn(
                expandFrom = Alignment.TopStart,
                animationSpec = tween(380)
            ) + fadeIn(animationSpec = tween(480)),
            exit = shrinkOut(
                shrinkTowards = Alignment.TopStart,
                animationSpec = tween(380)
            ) + fadeOut(animationSpec = tween(480)),
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
                .zIndex(5f)
        ) {
            LayersView(
                viewModel.mapType.collectAsState(),
                onLayerSelected = viewModel::onLayerSelected
            ) { onMapLayersSelectionChanged(false) }
        }

        NewPlaceDialog(dialogState = newPlaceDialog, onDismiss = onNewPlaceDialogDismiss)
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("PotentialBehaviorOverride", "MissingPermission")
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
) {
    val viewModel: MapViewModel = koinViewModel()
    val map = rememberMapViewWithLifecycle()
    val userPreferences: UserPreferences = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val darkTheme = isAppInDarkTheme()

    val showHiddenPlaces by userPreferences.shouldShowHiddenPlacesOnMap.collectAsState(true)
    val mapType by viewModel.mapType.collectAsState()
    val context = LocalContext.current
    val markers by viewModel.mapMarkers.collectAsState()

    val markersToShow by remember(markers, showHiddenPlaces) {
        mutableStateOf(
            if (showHiddenPlaces) markers
            else markers.filter { it.visible })
    }

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    var isMapVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isMapVisible,
        enter = fadeIn(), exit = fadeOut()
    ) {
        AndroidView(
            { map },
            modifier = modifier
                .fillMaxSize()
                .zIndex(-1.0f)
        ) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()

                //Map styles: https://mapstyle.withgoogle.com
                if (darkTheme) {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style_fishing_night
                        )
                    )
                } else {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style_fishing
                        )
                    )
                }

                googleMap.clear()
                markersToShow.forEach {
                    val position = LatLng(it.latitude, it.longitude)
                    val markerColor = Color(it.markerColor)
                    val hue = getHue(
                        red = markerColor.red,
                        green = markerColor.green,
                        blue = markerColor.blue
                    )
                    val marker = googleMap
                        .addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(it.title)
                                .icon(BitmapDescriptorFactory.defaultMarker(hue))

                        )
                    marker?.tag = it.id
                }
                googleMap.setOnCameraMoveStartedListener {
                    viewModel.setCameraMoveState(CameraMoveState.MoveStart)
                }
                googleMap.setOnCameraMoveListener {
                    viewModel.onCameraMove(
                        googleMap.cameraPosition.target,
                        googleMap.cameraPosition.zoom,
                        googleMap.cameraPosition.bearing
                    )
                }
                googleMap.setOnCameraIdleListener {
                    viewModel.setCameraMoveState(CameraMoveState.MoveFinish)
                    viewModel.saveLastCameraPosition()
                }
                googleMap.setOnMarkerClickListener { marker ->
                    viewModel.onMarkerClicked(
                        markers.find { it.id == marker.tag },
                    )
                    true
                }
                googleMap.setOnMapClickListener {
                    viewModel.resetMapUiState()
                    return@setOnMapClickListener
                }

                googleMap.uiSettings.isCompassEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
            }
        }
    }

    LaunchedEffect(map, permissionsState.allPermissionsGranted) {
        val googleMap = map.awaitMap()
        checkLocationPermissions(context)
        googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
    }

    LaunchedEffect(Unit) {
        viewModel.newMapCameraPosition.collectLatest {
            moveCameraToLocation(this, map, it.first, it.second, it.third)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.firstCameraPosition.collectLatest {
            it?.let { setCameraPosition(this, map, it.first, it.second, it.third) }
        }
    }

    LaunchedEffect(mapType) {
        val googleMap = map.awaitMap()
        googleMap.mapType = mapType
    }

    DisposableEffect(map) {
        isMapVisible = true
        viewModel.getLastLocation()

        onDispose { viewModel.saveLastCameraPosition() }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalPermissionsApi
@Composable
fun LocationPermissionDialog(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onCloseCallback: () -> Unit = { },
) {
    var isDialogOpen by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    if (isDialogOpen) {
        GrantLocationPermissionsDialog(
            onDismiss = {
                isDialogOpen = false
                onCloseCallback()
            },
            onNegativeClick = {
                isDialogOpen = false
                onCloseCallback()
            },
            onPositiveClick = {
                isDialogOpen = false
                if (permissionsState.shouldShowRationale) {
                    SnackbarManager.showMessage(R.string.location_permission_denied)
                    onCloseCallback()
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
                permissionsState.launchMultiplePermissionRequest()
                onCloseCallback()
            },
            onDontAskClick = {
                isDialogOpen = false
                SnackbarManager.showMessage(R.string.location_dont_ask)
                coroutineScope.launch {
                    userPreferences.saveLocationPermissionStatus(false)
                }
                onCloseCallback()
            }
        )
    }
}

@ExperimentalMaterial3Api
@Composable
fun MapFab(
    viewModel: MapViewModel,
    userSettings: UserPreferences,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
) {
    val state by viewModel.mapUiState.collectAsState()
    val useFastFabAdd by userSettings.useFabFastAdd.collectAsState(false)
    val context = LocalContext.current

    val adding_place = stringResource(R.string.adding_place_on_current_location)
    val permissions_required = stringResource(R.string.location_permissions_required)
    AnimatedVisibility(
        visible = state !is MapUiState.BottomSheetInfoMode,
        exit = fadeOut(),
        enter = fadeIn()
    ) {

        FishingFab(
            modifier = Modifier
                .animateContentSize()
                .padding(bottom = defaultFabBottomPadding),
            onClick = onClick,
            onLongPress = {
                if (state == MapUiState.NormalMode && useFastFabAdd) {
                    if (!checkLocationPermissions(context)) {
                        Toast.makeText(context, adding_place, Toast.LENGTH_SHORT).show()
                        onLongPress()
                    } else Toast.makeText(context, permissions_required, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        ) {
            AnimatedVisibility(state is MapUiState.NormalMode) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_location_24),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
            AnimatedVisibility(state is MapUiState.PlaceSelectMode) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_check_24),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishingFab(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor.copy(alpha = 1f),
        shadowElevation = 6.dp,
    ) {
        val ripple = LocalIndication.current
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = FabSize, minHeight = FabSize)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple,
                    enabled = true,
                    role = Role.Button,
                    onClick = onClick,
                    onDoubleClick = { },
                    onLongClick = onLongPress
                ),
            contentAlignment = Alignment.Center
        ) { content() }
    }
}

val FabSize = 56.dp

