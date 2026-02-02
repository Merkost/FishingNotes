package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsOff
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
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mobileprism.fishing.ui.home.views.SettingsCheckbox
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.shimmer
import com.google.accompanist.placeholder.material3.placeholder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.ui.home.views.SettingsHeader
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.theme.RedGoogleChrome
import com.mobileprism.fishing.ui.theme.secondaryFigmaColor
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.viewmodels.MapViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@ExperimentalMaterial3Api
@Composable
fun MapScaffold(
    mapUiState: MapUiState,
    modifier: Modifier = Modifier,
    onDismissCard: () -> Unit = {},
    fab: @Composable (() -> Unit)?,
    bottomCard: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }

    // Reset offset when card becomes visible
    LaunchedEffect(mapUiState) {
        if (mapUiState is MapUiState.BottomSheetInfoMode) {
            offsetY.snapTo(0f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        // Bottom card slides in from the bottom, swipeable to dismiss
        androidx.compose.animation.AnimatedVisibility(
            visible = mapUiState is MapUiState.BottomSheetInfoMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300)),
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
        SettingsHeader(stringResource(R.string.settings))
        SettingsCheckbox(
            icon = {
                Icon(
                    Icons.Default.Visibility, Icons.Default.Visibility.name,
                    tint = color.value
                )
            },
            title = { Text(text = stringResource(R.string.hidden_places)) },
            subtitle = { Text(text = stringResource(R.string.show_hidden_places)) },
            onCheckedChange = { newValue ->
                coroutineScope.launch { mapPreferences.saveMapHiddenPlaces(newValue) }
            },
            checked = showHiddenPlaces
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyLocationButton(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val locationManager: LocationManager = koinInject()
    var locationDialogIsShowing by remember { mutableStateOf(false) }
    val shouldShowPermissions by userPreferences.shouldShowLocationPermission.collectAsState(false)
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    if (locationDialogIsShowing) {
        if (shouldShowPermissions) {
            LocationPermissionDialog(userPreferences = userPreferences) {
                checkLocationPermissions(context)
                locationDialogIsShowing = false
            }
        } else SnackbarManager.showMessage(R.string.location_permission_denied)
    }

    val color = animateColorAsState(
        when {
            !shouldShowPermissions || !permissionsState.allPermissionsGranted -> {
                RedGoogleChrome
            }

            else -> {
                MaterialTheme.colorScheme.onSurface
            }
        }
    )

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = {
                when (permissionsState.allPermissionsGranted) {
                    true -> {
                        locationManager.checkGPSEnabled(context as MainActivity)
                        { onClick() }
                    }

                    false -> {
                        locationDialogIsShowing = true
                    }
                }
            }
        ) {
            Icon(
                if (!shouldShowPermissions) Icons.Default.GpsOff
                else Icons.Default.MyLocation,
                stringResource(R.string.my_location),
                tint = color.value
            )
        }
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
        Card(
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                onClick = { onClick() }) {
                Icon(
                    painterResource(
                        if (mapBearing.value > 356f ||
                            mapBearing.value < 4f
                        ) R.drawable.north
                        else R.drawable.gps
                    ),
                    stringResource(R.string.compass),
                    modifier = Modifier
                        .rotate(1f - mapBearing.value)
                        .fillMaxSize()
                )
            }
        }
    }

}

@Composable
fun MapZoomInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = { onClick() }) {
            Icon(
                Icons.Default.Add,
                Icons.Default.Add.name,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MapZoomOutButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = { onClick() }) {
            Icon(
                Icons.Default.Remove,
                Icons.Default.Remove.name,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MapLayersButton(modifier: Modifier, onLayersSelectionOpen: () -> Unit) {
    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = onLayersSelectionOpen
        ) {
            Icon(painterResource(R.drawable.ic_baseline_layers_24), stringResource(R.string.layers))
        }
    }
}

@Composable
fun LayersView(
    mapType: State<Int>,
    onLayerSelected: (Int) -> Unit,
    onCloseMapSelection: () -> Unit
) {
    val context = LocalContext.current
    val analyticsTracker = LocalAnalytics.current

    DisposableEffect(context) {
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
                Text(stringResource(R.string.map_type))
                IconButton(
                    onClick = onCloseMapSelection,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, stringResource(R.string.close))
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MapLayerItem(
                    currentMapType = mapType.value,
                    layer = MapTypes.roadmap,
                    painter = painterResource(R.drawable.ic_map_default),
                    name = stringResource(R.string.roadmap),
                    onLayerSelected = onLayerSelected
                )
                MapLayerItem(
                    currentMapType = mapType.value,
                    layer = MapTypes.hybrid,
                    painter = painterResource(R.drawable.ic_map_satellite),
                    name = stringResource(R.string.satellite),
                    onLayerSelected = onLayerSelected
                )
                MapLayerItem(
                    currentMapType = mapType.value,
                    layer = MapTypes.terrain,
                    painter = painterResource(R.drawable.ic_map_terrain),
                    name = stringResource(R.string.terrain),
                    onLayerSelected = onLayerSelected
                )
            }
        }
    }
}

@Composable
fun MapLayerItem(
    currentMapType: Int,
    layer: Int,
    painter: Painter,
    name: String,
    onLayerSelected: (Int) -> Unit
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
        modifier = modifier.size(40.dp)
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
fun PointerIcon(
    pointerState: PointerState,
    modifier: Modifier = Modifier,
) {
    var isFirstTimeCalled by remember { mutableStateOf(false) }

    val darkTheme = isSystemInDarkTheme()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(if (darkTheme) R.raw.marker_night else R.raw.marker)
    )
    val lottieAnimatable = rememberLottieAnimatable()

    val startMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(0, 50))
    }
    val finishMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(50, 82))
    }

    LaunchedEffect(isFirstTimeCalled) {
        lottieAnimatable.animate(
            composition,
            iteration = 1,
            continueFromPreviousAnimate = true,
            clipSpec = startMinMaxFrame,
        )
    }

    LaunchedEffect(pointerState) {
        if (pointerState == PointerState.ShowMarker) {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = true,
                clipSpec = startMinMaxFrame,
            )
        } else {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = false,
                clipSpec = finishMinMaxFrame,
            )
        }
    }

    LottieAnimation(
        modifier = modifier.size(128.dp),
        composition = composition,
        progress = lottieAnimatable.progress
    )

    isFirstTimeCalled = true

}

@Composable
fun FishLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fish_loading))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun PlaceTileView(
    modifier: Modifier,
) {
    LocalContext.current
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
            highlight = PlaceholderHighlight.shimmer(
                highlightColor = MaterialTheme.colorScheme.surface,
            ),
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
                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = stringResource(R.string.marker_icon),
                tint = pointerIconColor,
                modifier = Modifier
                    .size(30.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                selectedPlace.value.ifEmpty { stringResource(R.string.searching) },
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
    val context = LocalContext.current

    LaunchedEffect(geocoderResult) {
        geocoderResult.let {
            when (it) {
                is GeocoderResult.Success -> {
                    setPlaceName(it.placeName)
                }

                GeocoderResult.NoNamePlace -> {
                    setPlaceName(context.getString(R.string.unnamed_place))
                }

                GeocoderResult.Failed -> {
                    setPlaceName(context.getString(R.string.cant_recognize_place))
                }

                GeocoderResult.InProgress -> {
                    setPlaceName("")
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
@ExperimentalPermissionsApi
fun GrantLocationPermissionsDialog(
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDontAskClick: () -> Unit
) {

    DefaultDialog(
        primaryText = stringResource(R.string.location_permission_dialog),
        neutralButtonText = stringResource(id = R.string.dont_ask_again),
        onNeutralClick = onDontAskClick,
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = onNegativeClick,
        positiveButtonText = stringResource(id = R.string.ok_button),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieMyLocation(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieMyLocation(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.my_location))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
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