package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.rememberConnectionState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val NoOpPhoto: (String) -> Unit = {}

@Composable
fun ItemPhoto(
    photo: String,
    clickedPhoto: (String) -> Unit,
    deletedPhoto: (String) -> Unit,
    deleteEnabled: Boolean = true
) {
    val context = LocalPlatformContext.current

    val fullScreenPhoto = remember {
        mutableStateOf<String?>(null)
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(4.dp)
    ) {

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo)
                .size(Size(300, 300))
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .clickable {
                    clickedPhoto(photo)
                    fullScreenPhoto.value = photo
                },
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Medium,
            loading = {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        )
        if (deleteEnabled) {
            Surface(
                color = FishingTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = FishingTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.delete_photo),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { deletedPhoto(photo) })
            }
        }
    }

    AnimatedVisibility(fullScreenPhoto.value != null) {
        FullScreenPhoto(fullScreenPhoto)
    }
}

@Composable
fun PhotosView(
    modifier: Modifier = Modifier,
    photos: List<String>,
    onEditClick: () -> Unit
) {
    val connectionState by rememberConnectionState()

    val tempPhotosState = remember { mutableStateListOf<String>() }

    LaunchedEffect(key1 = photos) {
        tempPhotosState.apply {
            clear()
            addAll(photos)
        }
    }

    Column(modifier = modifier) {
        MaxCounterView(
            modifier = Modifier
                .align(Alignment.End)
                .padding(4.dp),
            count = tempPhotosState.size,
            maxCount = MAX_PHOTOS,
            icon = painterResource(Res.drawable.ic_baseline_photo_24)
        )
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (tempPhotosState.isNotEmpty()) {
                if (connectionState is ConnectionState.Available) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.Center) {
                            items(items = tempPhotosState) {
                                ItemCatchPhotoView(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    photo = it
                                )
                            }
                        }
                        AppButton(
                            text = stringResource(Res.string.edit),
                            onClick = onEditClick,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp),
                            style = AppButtonStyle.Text,
                        )
                    }
                } else {
                    NoContentView(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(Res.string.photos_not_available),
                        icon = painterResource(Res.drawable.ic_no_internet)
                    )
                }
            } else {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoContentView(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.no_photos_added),
                        icon = painterResource(Res.drawable.ic_no_photos)
                    )
                    AppButton(
                        text = stringResource(Res.string.add_photo),
                        onClick = onEditClick,
                        style = AppButtonStyle.Outlined,
                        leadingIcon = painterResource(Res.drawable.ic_baseline_add_photo_alternate_24),
                    )
                }
            }
        }
    }
}

@Composable
fun NewCatchPhotoView(
    modifier: Modifier = Modifier,
    photos: List<String>,
    onDelete: (String) -> Unit,
) {
    val connectionState by rememberConnectionState()

    val tempPhotosState = remember { mutableStateListOf<String>() }


    LaunchedEffect(key1 = photos) {
        tempPhotosState.apply {
            clear()
            addAll(photos)
        }
    }

    Column(
        modifier = modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        if (connectionState is ConnectionState.Available) {
            if (tempPhotosState.isNotEmpty()) {
                LazyVerticalGrid(
                    modifier = Modifier,
                    columns = GridCells.Fixed(MAX_PHOTOS),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(tempPhotosState) {
                        FullSizePhotoView(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            photo = it,
                            clickedPhoto = NoOpPhoto,
                            deletedPhoto = { photo -> onDelete(photo) }
                        )
                    }
                }
            } else {
                NoContentView(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.no_photos_added),
                    icon = painterResource(Res.drawable.ic_no_photos)
                )
            }
        } else {
            NoContentView(
                modifier = Modifier.padding(8.dp),
                text = stringResource(Res.string.photos_not_available),
                icon = painterResource(Res.drawable.ic_no_internet)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun FullSizePhotoView(
    modifier: Modifier = Modifier,
    photo: String,
    clickedPhoto: (String) -> Unit,
    deletedPhoto: (String) -> Unit,
    deleteEnabled: Boolean = true
) {
    val fullScreenPhoto = remember {
        mutableStateOf<String?>(null)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .heightIn(max = 256.dp)
    ) {

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photo)
                .size(Size(600, 600))
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.Center)
                .clip(RoundedCornerShape(5.dp))
                .clickable {
                    clickedPhoto(photo)
                    fullScreenPhoto.value = photo
                },
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Medium,
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(64.dp)
                        .align(Alignment.Center)
                )
            }
        )
        if (deleteEnabled) {
            Surface(
                color = FishingTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = FishingTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.delete_photo),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { deletedPhoto(photo) })
            }
        }
    }

    AnimatedVisibility(fullScreenPhoto.value != null) {
        FullScreenPhoto(fullScreenPhoto)
    }
}

@Composable
fun ItemCatchPhotoView(
    modifier: Modifier = Modifier,
    photo: String
) {
    val context = LocalPlatformContext.current
    val fullScreenPhoto = remember {
        mutableStateOf<String?>(null)
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(photo)
            .size(Size(300, 300))
            .build(),
        contentDescription = null,
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable { fullScreenPhoto.value = photo },
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Medium,
        loading = {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
    )

    AnimatedVisibility(fullScreenPhoto.value != null) {
        FullScreenPhoto(fullScreenPhoto)
    }
}


@Composable
fun FullScreenPhoto(photo: MutableState<String?>) {

    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(0f) }
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()
    val dismissOffsetY = remember { Animatable(0f) }
    val alpha = 0.8f - abs(dismissOffsetY.value).div(600)
    val backgroundColor = animateColorAsState(
        targetValue = FishingTheme.colorScheme.scrim.copy(if (alpha < 0) 0f else alpha)
    )

    Dialog(
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true, usePlatformDefaultWidth = false
        ),
        onDismissRequest = { photo.value = null }) {
        Surface(
            Modifier
                .fillMaxSize(), color = backgroundColor.value
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photo.value)
                    .crossfade(true)
                    .build(),
                filterQuality = FilterQuality.High,
                loading = {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            offsetX.value.roundToInt(),
                            (offsetY.value + dismissOffsetY.value).roundToInt()
                        )
                    }
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        rotationZ = rotationState.value
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            scale.value = (scale.value * zoom).coerceIn(0.5f, 5f)
                            rotationState.value += rotation
                            if (scale.value > 1f) {
                                offsetX.value += pan.x
                                offsetY.value += pan.y
                            } else {
                                coroutineScope.launch {
                                    dismissOffsetY.snapTo(dismissOffsetY.value + pan.y)
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale.value = 1f
                                rotationState.value = 0f
                                offsetX.value = 0f
                                offsetY.value = 0f
                                coroutineScope.launch {
                                    dismissOffsetY.snapTo(0f)
                                }
                            },
                            onTap = {
                                if (scale.value <= 1f) {
                                    photo.value = null
                                }
                            }
                        )
                    }
                    .then(
                        if (scale.value <= 1f) {
                            Modifier.draggable(
                                state = rememberDraggableState { delta ->
                                    coroutineScope.launch {
                                        dismissOffsetY.snapTo(dismissOffsetY.value + delta)
                                    }
                                },
                                orientation = Orientation.Vertical,
                                onDragStopped = {
                                    if (abs(dismissOffsetY.value) >= 400f) {
                                        photo.value = null
                                    } else {
                                        coroutineScope.launch {
                                            dismissOffsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(
                                                    durationMillis = 400,
                                                    delayMillis = 0
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        } else Modifier
                    ),
                contentDescription = stringResource(Res.string.catch_photo)
            )
        }

    }
}
