package com.mobileprism.fishing.ui.home.map

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileprism.fishing.ui.utils.AnimatedResource
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.FloatingControlSurface
import com.mobileprism.fishing.ui.home.views.FloatingIconButton
import com.mobileprism.fishing.ui.home.views.SettingsCheckbox
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.views.SettingsHeader
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.rememberAppSettingsOpener
import com.mobileprism.fishing.ui.utils.rememberLocationPermissionGranted
import com.mobileprism.fishing.ui.utils.rememberPermissionsController
import com.mobileprism.fishing.viewmodels.MapViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@ExperimentalMaterial3Api
@Composable
fun MapScaffold(
    mapUiState: MapUiState,
    modifier: Modifier = Modifier,
    onDismissCard: () -> Unit = {},
    fab: @Composable (() -> Unit)?,
    bottomCard: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = mapUiState is MapUiState.BottomSheetInfoMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(200)),
        ) {
            DragDismissContainer(onDismiss = onDismissCard) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DragHandle(modifier = Modifier.padding(bottom = Spacing.xs))
                    bottomCard()
                }
            }
        }

        fab?.let { fabContent ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.lg)
            ) {
                fabContent()
            }
        }
    }
}

@Composable
fun MapModalBottomSheet(
    mapPreferences: UserPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    val showHiddenPlaces by mapPreferences.shouldShowHiddenPlacesOnMap.collectAsState(false)

    val color = animateColorAsState(
        targetValue = if (showHiddenPlaces) {
            FishingTheme.colorScheme.onSurface
        } else {
            FishingTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(800)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsHeader(stringResource(Res.string.settings))
        SettingsCheckbox(
            icon = {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = color.value
                )
            },
            title = { Text(text = stringResource(Res.string.hidden_places)) },
            subtitle = { Text(text = stringResource(Res.string.show_hidden_places)) },
            onCheckedChange = { newValue ->
                coroutineScope.launch { mapPreferences.saveMapHiddenPlaces(newValue) }
            },
            checked = showHiddenPlaces
        )
    }
}

@Composable
fun MyLocationButton(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    state: MyLocationButtonState,
    onGpsDisabled: () -> Unit,
    onClick: () -> Unit,
) {
    val checkGPS = rememberGPSChecker()
    var locationDialogIsShowing by remember { mutableStateOf(false) }
    val shouldShowPermissions by userPreferences.shouldShowLocationPermission.collectAsState(true)
    val permissionsController = rememberPermissionsController()
    val locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)
    val openAppSettings = rememberAppSettingsOpener()

    if (locationDialogIsShowing) {
        if (shouldShowPermissions) {
            LocationPermissionDialog(
                userPreferences = userPreferences,
                onPermissionGranted = {
                    checkGPS(onClick, onGpsDisabled)
                },
                onCloseCallback = {
                    locationDialogIsShowing = false
                },
            )
        }
    }

    val effectiveState = when {
        !locationPermissionGranted && !shouldShowPermissions -> MyLocationButtonState.PermissionBlocked
        !locationPermissionGranted -> MyLocationButtonState.NeedsPermission
        else -> state
    }
    val icon = when (effectiveState) {
        MyLocationButtonState.Ready -> Icons.Default.MyLocation
        MyLocationButtonState.Searching -> Icons.Default.LocationSearching
        MyLocationButtonState.NeedsPermission -> Icons.AutoMirrored.Filled.HelpOutline
        MyLocationButtonState.PermissionBlocked,
        MyLocationButtonState.GpsDisabled -> Icons.Default.GpsOff
        MyLocationButtonState.Unavailable -> Icons.Default.ErrorOutline
    }
    val contentColor = animateColorAsState(
        targetValue = when (effectiveState) {
            MyLocationButtonState.Ready -> FishingTheme.colorScheme.onSurface
            MyLocationButtonState.Searching -> FishingTheme.colorScheme.primary
            MyLocationButtonState.NeedsPermission -> FishingTheme.colorScheme.tertiary
            MyLocationButtonState.PermissionBlocked,
            MyLocationButtonState.GpsDisabled,
            MyLocationButtonState.Unavailable -> FishingTheme.colorScheme.error
        },
        animationSpec = tween(250)
    )
    val containerColor = animateColorAsState(
        targetValue = when (effectiveState) {
            MyLocationButtonState.Ready -> Color.Transparent
            MyLocationButtonState.Searching -> FishingTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            MyLocationButtonState.NeedsPermission -> FishingTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
            MyLocationButtonState.PermissionBlocked,
            MyLocationButtonState.GpsDisabled,
            MyLocationButtonState.Unavailable -> FishingTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        },
        animationSpec = tween(250)
    )

    FloatingControlSurface(modifier = modifier) {
        FloatingIconButton(
            icon = icon,
            contentDescription = stringResource(Res.string.my_location),
            tint = contentColor.value,
            onClick = {
                if (effectiveState == MyLocationButtonState.Searching) return@FloatingIconButton
                when (locationPermissionGranted) {
                    true -> {
                        checkGPS(onClick, onGpsDisabled)
                    }

                    false -> {
                        if (shouldShowPermissions) {
                            locationDialogIsShowing = true
                        } else {
                            SnackbarManager.showMessage(
                                messageTextId = Res.string.location_permission_denied,
                                snackbarAction = SnackbarAction(
                                    textId = Res.string.goto_app_settings,
                                    action = openAppSettings,
                                ),
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
fun CompassButton(
    modifier: Modifier = Modifier,
    mapBearing: State<Float>,
    onClick: () -> Unit
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = mapBearing.value < 356f && mapBearing.value > 4f,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(delayMillis = 3000, durationMillis = 1000))
    ) {
        FloatingControlSurface {
            FloatingIconButton(
                painter = painterResource(
                    if (mapBearing.value > 356f || mapBearing.value < 4f) Res.drawable.north
                    else Res.drawable.gps
                ),
                contentDescription = stringResource(Res.string.compass),
                iconModifier = Modifier.rotate(1f - mapBearing.value),
                onClick = { onClick() },
            )
        }
    }

}

@Composable
fun MapZoomInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingControlSurface(modifier = modifier) {
        FloatingIconButton(
            icon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.zoom_in),
            onClick = { onClick() },
        )
    }
}

@Composable
fun MapZoomOutButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingControlSurface(modifier = modifier) {
        FloatingIconButton(
            icon = Icons.Default.Remove,
            contentDescription = stringResource(Res.string.zoom_out),
            onClick = { onClick() },
        )
    }
}

@Composable
fun MapControlsLeftPill(
    modifier: Modifier = Modifier,
    onLayersClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    FloatingControlSurface(modifier = modifier) {
        Row(
            modifier = Modifier.padding(Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.none),
        ) {
            FloatingIconButton(
                painter = painterResource(Res.drawable.ic_baseline_layers_24),
                contentDescription = stringResource(Res.string.layers),
                onClick = onLayersClick,
            )
            FloatingIconButton(
                icon = Icons.Default.Settings,
                contentDescription = stringResource(Res.string.map_settings),
                onClick = onSettingsClick,
            )
        }
    }
}


@Composable
fun LayersView(
    mapType: State<AppMapType>,
    onLayerSelected: (AppMapType) -> Unit,
    onCloseMapSelection: () -> Unit
) {
    val analyticsTracker = LocalAnalytics.current

    DisposableEffect(Unit) {
        analyticsTracker.logEvent(AnalyticsEvent.MapLayers)
        onDispose {}
    }

    Card(
        shape = FishingTheme.shapes.medium,
        modifier = Modifier
            .width(250.dp)
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.xxs)
                .padding(bottom = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(Res.string.map_type))
                FloatingIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.close),
                    onClick = onCloseMapSelection,
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MapLayerItem(
                    currentMapType = mapType.value,
                    layer = AppMapType.Roadmap,
                    painter = painterResource(Res.drawable.ic_map_default),
                    name = stringResource(Res.string.roadmap),
                    onLayerSelected = onLayerSelected
                )
                MapLayerItem(
                    currentMapType = mapType.value,
                    layer = AppMapType.Hybrid,
                    painter = painterResource(Res.drawable.ic_map_satellite),
                    name = stringResource(Res.string.satellite),
                    onLayerSelected = onLayerSelected
                )
                MapLayerItem(
                    currentMapType = mapType.value,
                    layer = AppMapType.Terrain,
                    painter = painterResource(Res.drawable.ic_map_terrain),
                    name = stringResource(Res.string.terrain),
                    onLayerSelected = onLayerSelected
                )
            }
        }
    }
}

@Composable
fun MapLayerItem(
    currentMapType: AppMapType,
    layer: AppMapType,
    painter: Painter,
    name: String,
    onLayerSelected: (AppMapType) -> Unit
) {
    val isSelected = currentMapType == layer
    val animatedColor by animateColorAsState(
        if (isSelected) FishingTheme.colorScheme.primary
        else FishingTheme.colorScheme.outlineVariant,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(70.dp)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = animatedColor,
                    shape = FishingTheme.shapes.medium
                ),
            shape = FishingTheme.shapes.medium,
            onClick = { onLayerSelected(layer) }
        ) {
            Image(
                painter = painter,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = name,
            fontSize = 12.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}


@Composable
fun FishLoading(modifier: Modifier) {
    AnimatedResource("fish_loading", modifier, iterations = 1)
}

@Composable
fun PlaceTileView(
    modifier: Modifier,
) {
    val viewModel: MapViewModel = koinViewModel()
    val placeTileViewNameState by viewModel.placeTileViewNameState.collectAsState()

    val selectedPlace = remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        viewModel.getPlaceTileViewName()
        onDispose { viewModel.cancelPlaceTileNameJob() }
    }

    SetPlaceNameResultListener(placeTileViewNameState.geocoderResult) { newPlaceName ->
        selectedPlace.value = newPlaceName
    }

    GeocoderResultChip(
        result = placeTileViewNameState.geocoderResult,
        placeName = selectedPlace.value,
        modifier = modifier,
    )
}

@Composable
fun SetPlaceNameResultListener(geocoderResult: GeocoderResult, setPlaceName: (String) -> Unit) {
    val unnamedPlace = stringResource(Res.string.unnamed_place)
    val cantRecognizePlace = stringResource(Res.string.cant_recognize_place)

    LaunchedEffect(geocoderResult) {
        geocoderResult.let {
            when (it) {
                is GeocoderResult.Success -> {
                    setPlaceName(it.placeName)
                }

                GeocoderResult.NoNamePlace -> {
                    setPlaceName(unnamedPlace)
                }

                GeocoderResult.Failed -> {
                    setPlaceName(cantRecognizePlace)
                }

                GeocoderResult.InProgress -> {
                    setPlaceName("")
                }
            }
        }
    }
}

