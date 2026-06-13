package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
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
import com.mobileprism.fishing.ui.utils.placeholder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.views.SettingsHeader
import com.mobileprism.fishing.ui.theme.secondaryFigmaColor
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
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }

    LaunchedEffect(mapUiState) {
        if (mapUiState is MapUiState.BottomSheetInfoMode) {
            offsetY.snapTo(0f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = mapUiState is MapUiState.BottomSheetInfoMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250)),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(200)),
        ) {
            Column(
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.value.toInt().coerceAtLeast(0)) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetY.value > dismissThresholdPx) {
                                    onDismissCard()
                                } else {
                                    coroutineScope.launch {
                                        offsetY.animateTo(0f, animationSpec = tween(200))
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetY.animateTo(0f, animationSpec = tween(200))
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetY.snapTo(offsetY.value + dragAmount)
                                }
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetLine(modifier = Modifier.padding(bottom = 4.dp))
                bottomCard()
            }
        }

        fab?.let { fabContent ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
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
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(800)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsHeader(stringResource(Res.string.settings))
        SettingsCheckbox(
            icon = {
                Icon(
                    Icons.Default.Visibility, Icons.Default.Visibility.name,
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
            MyLocationButtonState.Ready -> MaterialTheme.colorScheme.onSurface
            MyLocationButtonState.Searching -> MaterialTheme.colorScheme.primary
            MyLocationButtonState.NeedsPermission -> MaterialTheme.colorScheme.tertiary
            MyLocationButtonState.PermissionBlocked,
            MyLocationButtonState.GpsDisabled,
            MyLocationButtonState.Unavailable -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(250)
    )
    val containerColor = animateColorAsState(
        targetValue = when (effectiveState) {
            MyLocationButtonState.Ready -> Color.Transparent
            MyLocationButtonState.Searching -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            MyLocationButtonState.NeedsPermission -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
            MyLocationButtonState.PermissionBlocked,
            MyLocationButtonState.GpsDisabled,
            MyLocationButtonState.Unavailable -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
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
fun MapLayersButton(modifier: Modifier, onLayersSelectionOpen: () -> Unit) {
    Card(
        shape = CircleShape,
        modifier = modifier.size(48.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = onLayersSelectionOpen
        ) {
            Icon(painterResource(Res.drawable.ic_baseline_layers_24), stringResource(Res.string.layers))
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
                Text(stringResource(Res.string.map_type))
                IconButton(
                    onClick = onCloseMapSelection,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, stringResource(Res.string.close))
                }
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
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
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
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            onClick = { onLayerSelected(layer) }
        ) {
            Image(
                painter = painter,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun MapSettingsButton(
    modifier: Modifier,
    onCLick: () -> Unit,
) {

    Card(
        shape = CircleShape,
        modifier = modifier.size(48.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = onCLick
        ) {
            Icon(
                Icons.Default.Settings, Icons.Default.Settings.name,
            )
        }
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

    val shimmerModifier = if (selectedPlace.value.isNotBlank()) Modifier else
        Modifier.placeholder(
            visible = true,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = CircleShape,
        )
    val pointerIconColor by animateColorAsState(
        if (selectedPlace.value.isNotBlank()) secondaryFigmaColor
        else MaterialTheme.colorScheme.outlineVariant
    )
    val textColor by animateColorAsState(
        if (selectedPlace.value.isNotBlank()) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.outlineVariant
    )


    Card(
        shape = RoundedCornerShape(size = 20.dp),
        modifier = modifier
            .heightIn(min = 40.dp, max = 80.dp)
            .widthIn(max = 240.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                contentDescription = stringResource(Res.string.marker_icon),
                tint = pointerIconColor,
                modifier = Modifier
                    .size(30.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                selectedPlace.value.ifEmpty { stringResource(Res.string.searching) },
                overflow = TextOverflow.Ellipsis,
                color = textColor,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .then(shimmerModifier)
            )
            Spacer(Modifier.size(4.dp))
        }
    }
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

@Composable
fun BottomSheetLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 25.dp, height = 3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}
