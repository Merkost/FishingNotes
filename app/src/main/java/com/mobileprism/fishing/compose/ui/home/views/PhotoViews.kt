package com.mobileprism.fishing.compose.ui.home.views

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.home.catch_screen.addPhoto
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.currentConnectivityState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow
import com.mobileprism.fishing.utils.showToast
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
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

@OptIn(
    ExperimentalAnimationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class,
    com.google.accompanist.permissions.ExperimentalPermissionsApi::class
)
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

    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    val addPhotoState = rememberSaveable { mutableStateOf(false) }

    val choosePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { value ->
            if ((value.size + tempPhotosState.size) > MAX_PHOTOS) {
                showToast(context, context.getString(R.string.max_photos_allowed))
            }
            tempPhotosState.addAll(value)
        }

    LaunchedEffect(key1 = photos) {
        tempPhotosState.apply {
            clear()
            addAll(photos)
        }
    }

    Column(
        modifier = modifier
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (tempPhotosState.isNotEmpty()) {
            if (connectionState is ConnectionState.Available) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(items = tempPhotosState) {
                        FullSizePhotoView(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            photo = it,
                            clickedPhoto = {},
                            deletedPhoto = { photo -> onDelete(photo) }
                        )
                    }
                }

            } else {
                NoContentView(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.photos_not_available),
                    icon = painterResource(id = R.drawable.ic_no_internet)
                )
            }
        } else {
            NoContentView(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.no_photos_added),
                icon = painterResource(id = R.drawable.ic_no_photos)
            )
        }
    }
    if (addPhotoState.value) {
        LaunchedEffect(addPhotoState) {
            permissionState.launchPermissionRequest()
        }
        addPhoto(permissionState, addPhotoState, choosePhotoLauncher)
    }
}

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
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
            .wrapContentHeight()
    ) {

        AsyncImage(
            model = photo,
            contentDescription = null,
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .clip(RoundedCornerShape(5.dp))
                .clickable {
                    clickedPhoto(photo)
                    fullScreenPhoto.value = photo
                },
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low
        ) { state ->
            if (state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(64.dp)
                        .align(Alignment.Center)
                )
            } else {
                AsyncImageContent()
            }
        }
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

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ItemCatchPhotoView(
    modifier: Modifier = Modifier,
    photo: Uri
) {
    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    AsyncImage(
        model = photo,
        contentDescription = null,
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable { fullScreenPhoto.value = photo },
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Low
    ) { state ->
        if (state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            AsyncImageContent()
        }
    }

    AnimatedVisibility(fullScreenPhoto.value != null) {
        FullScreenPhoto(fullScreenPhoto)
    }
}


@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun FullScreenPhoto(photo: MutableState<Uri?>) {

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
            AsyncImage(
                model = photo.value,
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