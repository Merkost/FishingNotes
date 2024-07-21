package com.mobileprism.fishing.ui.home.views

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mobileprism.fishing.R
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.currentConnectivityState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ItemPhoto(
    photo: Uri,
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit,
    deleteEnabled: Boolean = true
) {

    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(4.dp)
    ) {

        SubcomposeAsyncImage(
            model = photo,
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
        ) /*{ state ->
            if (state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(64.dp)
                        .align(Alignment.Center)
                )
            } else {
                AsyncImageContent()
            }
        }*/// FIXME:
        if (deleteEnabled) {
            Surface(
                color = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = Color.White,
                    contentDescription = stringResource(R.string.delete_photo),
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
    photos: List<Uri>,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    val tempPhotosState = remember { mutableStateListOf<Uri>() }

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
                .padding(8.dp),
            count = tempPhotosState.size,
            maxCount = MAX_PHOTOS,
            icon = painterResource(id = R.drawable.ic_baseline_photo_24)
        )
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
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
                        DefaultButton(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp),
                            text = stringResource(id = R.string.edit),
                            onClick = onEditClick
                        )
                    }
                } else {
                    NoContentView(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.photos_not_available),
                        icon = painterResource(id = R.drawable.ic_no_internet)
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
                        text = stringResource(id = R.string.no_photos_added),
                        icon = painterResource(id = R.drawable.ic_no_photos)
                    )
                    DefaultButtonOutlined(
                        text = stringResource(id = R.string.add_photo),
                        icon = painterResource(id = R.drawable.ic_baseline_add_photo_alternate_24),
                        onClick = onEditClick
                    )
                }
            }
        }
    }
}

@Composable
fun NewCatchPhotoView(
    modifier: Modifier = Modifier,
    photos: List<Uri>,
    onDelete: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    val tempPhotosState = remember { mutableStateListOf<Uri>() }


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
                            clickedPhoto = {},
                            deletedPhoto = { photo -> onDelete(photo) }
                        )
                    }
                }
            } else {
                NoContentView(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.no_photos_added),
                    icon = painterResource(id = R.drawable.ic_no_photos)
                )
            }
        } else {
            NoContentView(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.photos_not_available),
                icon = painterResource(id = R.drawable.ic_no_internet)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun FullSizePhotoView(
    modifier: Modifier = Modifier,
    photo: Uri,
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit,
    deleteEnabled: Boolean = true
) {
    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .heightIn(max = 256.dp)
    ) {

        SubcomposeAsyncImage(
            model = photo,
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
                color = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = MaterialTheme.colors.onPrimary,
                    contentDescription = stringResource(R.string.delete_photo),
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
    photo: Uri
) {
    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    SubcomposeAsyncImage(
        model = photo,
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
fun FullScreenPhoto(photo: MutableState<Uri?>) {

    // TODO: add rotation and zoom
    /*val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(1f) }*/

    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val alpha = 0.8f - abs(offsetY.value).div(600)
    val backgroundColor = animateColorAsState(
        targetValue = Color.Black.copy(if (alpha < 0) 0f else alpha)
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
                model = photo.value,
                filterQuality = FilterQuality.High,
                loading = {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(0, offsetY.value.roundToInt())
                    }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch {
                                offsetY.snapTo(offsetY.value + delta)
                            }
                        },
                        orientation = Orientation.Vertical,
                        onDragStarted = {

                        },
                        onDragStopped = {

                            if (offsetY.value >= 400f || offsetY.value <= -400f) photo.value =
                                null else
                                coroutineScope.launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = 400,
                                            delayMillis = 0
                                        )
                                    )
                                }
                        }
                    )
                    .clickable {
                        photo.value = null
                    },
                contentDescription = stringResource(id = R.string.catch_photo)
            )
        }

    }
}