package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.theme.isAppInDarkTheme
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.ui.utils.PlatformBackHandler
import com.mobileprism.fishing.ui.utils.rememberLocationPermissionGranted
import com.mobileprism.fishing.ui.utils.rememberPermissionsController
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.defaultFabBottomPadding
import com.mobileprism.fishing.viewmodels.MapViewModel
import eu.buney.maps.*
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    addPlaceOnStart: Boolean = false,
    place: com.mobileprism.fishing.domain.entity.content.UserMapMarker?,
    upPress: () -> Unit,
) {
    val viewModel: MapViewModel = koinViewModel()
    LaunchedEffect(place) {
        viewModel.setPlace(place)
    }
    LaunchedEffect(addPlaceOnStart) {
        viewModel.setAddingPlace(addPlaceOnStart)
    }

    val mapUiState by viewModel.mapUiState.collectAsState()
    val mapMarkers by viewModel.mapMarkers.collectAsState()

    val analyticsTracker = LocalAnalytics.current
    val userPreferences: UserPreferences = koinInject()
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)

    val onboardingViewModel: com.mobileprism.fishing.viewmodels.OnboardingViewModel = koinViewModel()
    val onboardingCompleted by onboardingViewModel.hasCompletedOnboarding.collectAsState()
    val isCardDismissed by onboardingViewModel.isPromptCardDismissed.collectAsState()
    val showPromptCard = onboardingCompleted == true && mapMarkers.isEmpty() && !isCardDismissed

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showModalBottomSheet by rememberSaveable { mutableStateOf(false) }
    var newPlaceDialog by rememberSaveable { mutableStateOf(false) }
    var mapLayersSelection by rememberSaveable { mutableStateOf(false) }

    val noNamePlace = stringResource(Res.string.no_name_place)

    BackPressHandler(
        mapUiState = mapUiState,
        navController = navController,
        onBackPressedCallback = viewModel::resetMapUiState,
        upPress = upPress,
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

    if (newPlaceDialog) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.cancelAddNewMarker()
                newPlaceDialog = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = Constants.modalBottomSheetCorners,
        ) {
            NewPlaceBottomSheetContent(onDismiss = { newPlaceDialog = false })
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
                    onLongPress = { viewModel.quickAddPlace(name = noNamePlace) },
                    userSettings = userPreferences,
                )
            },
            bottomCard = {
                PlaceDetailsCard(
                    viewModel = viewModel,
                    navController = navController,
                    onAddCatch = { marker ->
                        navController.navigate(MainDestinations.NewCatch(place = marker))
                    },
                    onSaveCurrentPlace = { newPlaceDialog = true },
                )
            },
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
                place = place,
                showPromptCard = showPromptCard,
                onPromptCardDismiss = { onboardingViewModel.dismissPromptCard() },
                onPromptCardClick = { viewModel.setPlaceSelectionMode() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapControls(
    mapUiState: MapUiState,
    viewModel: MapViewModel,
    userPreferences: UserPreferences,
    useZoomButtons: Boolean,
    mapLayersSelection: Boolean,
    onMapLayersSelectionChanged: (Boolean) -> Unit,
    onMapSettingsClicked: () -> Unit,
    place: com.mobileprism.fishing.domain.entity.content.UserMapMarker? = null,
    showPromptCard: Boolean = false,
    onPromptCardDismiss: () -> Unit = {},
    onPromptCardClick: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MapLayout(modifier = Modifier.fillMaxSize(), place = place)

        if (mapLayersSelection) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .zIndex(4f)
                    .clickable { onMapLayersSelectionChanged(false) }
            ) {}
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.Top
            ) {
                MapControlsLeftPill(
                    onLayersClick = { onMapLayersSelectionChanged(true) },
                    onSettingsClick = onMapSettingsClicked,
                )

                Spacer(modifier = Modifier.weight(1f))
                AnimatedVisibility(
                    visible = mapUiState == MapUiState.PlaceSelectMode && !mapLayersSelection,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                ) {
                    PlaceTileView(modifier = Modifier.wrapContentSize().padding(horizontal = 8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))

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

            if (useZoomButtons) {
                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                    MapZoomInButton(onClick = viewModel::onZoomInClick)
                    Spacer(modifier = Modifier.height(8.dp))
                    MapZoomOutButton(onClick = viewModel::onZoomOutClick)
                }
            }

            FirstSpotPromptCard(
                visible = showPromptCard && mapUiState == MapUiState.NormalMode,
                onDismiss = onPromptCardDismiss,
                onClick = onPromptCardClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
            )

            AnimatedVisibility(
                visible = mapUiState == MapUiState.PlaceSelectMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 65.dp)
            ) {
                PointerAnimation(viewModel.placeTileViewNameState.collectAsState().value.pointerState)
            }
        }

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

    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
    place: com.mobileprism.fishing.domain.entity.content.UserMapMarker? = null,
) {
    val viewModel: MapViewModel = koinViewModel()
    val userPreferences: UserPreferences = koinInject()
    val darkTheme = isAppInDarkTheme()

    val showHiddenPlaces by userPreferences.shouldShowHiddenPlacesOnMap.collectAsState(true)
    val mapType by viewModel.mapType.collectAsState()
    val markers by viewModel.mapMarkers.collectAsState()

    val markersToShow by remember(markers, showHiddenPlaces) {
        mutableStateOf(
            if (showHiddenPlaces) markers
            else markers.filter { it.visible })
    }

    val permissionsController = rememberPermissionsController()
    val locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }.collect { isMoving ->
            if (isMoving) {
                viewModel.setCameraMoveState(CameraMoveState.MoveStart)
            } else {
                viewModel.setCameraMoveState(CameraMoveState.MoveFinish)
                viewModel.saveLastCameraPosition()
            }
        }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position }.collect { position ->
            viewModel.onCameraMove(
                position.target.latitude,
                position.target.longitude,
                position.zoom,
                position.bearing
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.newMapCameraPosition.collectLatest { cameraState ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        target = LatLng(cameraState.latitude, cameraState.longitude),
                        zoom = cameraState.zoom,
                        bearing = cameraState.bearing,
                        tilt = 0f
                    )
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.firstCameraPosition.collectLatest { cameraState ->
            cameraState?.let {
                cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            target = LatLng(it.latitude, it.longitude),
                            zoom = it.zoom,
                            bearing = it.bearing,
                            tilt = 0f
                        )
                    )
                )
            }
        }
    }

    LaunchedEffect(place) {
        place?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        target = LatLng(it.latitude, it.longitude),
                        zoom = DEFAULT_ZOOM,
                        bearing = 0f,
                        tilt = 0f
                    )
                )
            )
        }
    }

    val styleJson by produceState<String?>(null, darkTheme) {
        val styleFileName = if (darkTheme) "map_style_fishing_night" else "map_style_fishing"
        value = Res.readBytes("files/$styleFileName.json").decodeToString()
    }

    val mapProperties = remember(mapType, styleJson, locationPermissionGranted) {
        val base = MapProperties(
            mapType = mapType.toMapType(),
            isMyLocationEnabled = locationPermissionGranted,
        )
        styleJson?.let { base.copy(mapStyleOptions = MapStyleOptions.fromJson(it)) } ?: base
    }

    GoogleMap(
        modifier = modifier
            .fillMaxSize()
            .zIndex(-1.0f),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
        ),
        onMapClick = { viewModel.resetMapUiState() },
    ) {
        markersToShow.forEach { userMarker ->
            val position = LatLng(userMarker.latitude, userMarker.longitude)
            val markerColor = Color(userMarker.markerColor)
            val markerIcon = rememberComposeBitmapDescriptor(
                userMarker.markerColor, userMarker.catchesCount
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                        contentDescription = null,
                        tint = markerColor,
                        modifier = Modifier.size(36.dp)
                    )
                    if (userMarker.catchesCount > 0) {
                        androidx.compose.material3.Text(
                            text = userMarker.catchesCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFF6D00),
                                    shape = CircleShape,
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Marker(
                state = rememberUpdatedMarkerState(position = position),
                title = userMarker.title,
                icon = markerIcon,
                onClick = {
                    viewModel.onMarkerClicked(userMarker)
                    true
                }
            )
        }
    }

    DisposableEffect(Unit) {
        viewModel.getLastLocation()
        onDispose { viewModel.saveLastCameraPosition() }
    }
}

private fun AppMapType.toMapType(): MapType = when (this) {
    AppMapType.Roadmap -> MapType.NORMAL
    AppMapType.Satellite -> MapType.SATELLITE
    AppMapType.Hybrid -> MapType.HYBRID
    AppMapType.Terrain -> MapType.TERRAIN
}

@Composable
fun BackPressHandler(
    mapUiState: MapUiState,
    navController: NavController,
    onBackPressedCallback: () -> Unit,
    upPress: () -> Unit,
) {
    var lastPressed by remember { mutableStateOf(0L) }

    PlatformBackHandler(true) {
        when (mapUiState) {
            MapUiState.NormalMode -> {
                if (navController.navigateUp()) {
                    return@PlatformBackHandler
                } else {
                    val currentMillis = Clock.System.now().toEpochMilliseconds()
                    if (currentMillis - lastPressed < Constants.TIME_TO_EXIT) {
                        upPress()
                    } else {
                        SnackbarManager.showMessage(Res.string.app_exit_message)
                    }
                    lastPressed = currentMillis
                }
            }
            else -> onBackPressedCallback()
        }
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
    val permissionsController = rememberPermissionsController()
    val locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)

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
                    if (locationPermissionGranted) {
                        SnackbarManager.showMessage(Res.string.adding_place_on_current_location)
                        onLongPress()
                    } else {
                        SnackbarManager.showMessage(Res.string.location_permissions_required)
                    }
                }
            }
        ) {
            AnimatedVisibility(state is MapUiState.NormalMode) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_add_location_24),
                    contentDescription = stringResource(Res.string.new_place),
                    tint = Color.White,
                )
            }
            AnimatedVisibility(state is MapUiState.PlaceSelectMode) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_check_24),
                    contentDescription = stringResource(Res.string.new_place),
                    tint = Color.White,
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
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
    ) {
        val ripple = LocalIndication.current
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = FabSize, minHeight = FabSize)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
                    ),
                )
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
