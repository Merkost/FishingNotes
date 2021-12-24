package com.joesemper.fishing.compose.ui.home.views

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.home.catch_screen.AddPhotoDialog
import com.joesemper.fishing.compose.ui.home.catch_screen.ItemCatchPhotoView
import com.joesemper.fishing.utils.network.ConnectionState
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun PhotosView(
    modifier: Modifier = Modifier,
    photos: List<Uri>,
    onSavePhotos: (List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    val dialogState = remember { mutableStateOf(false) }

    val tempPhotosState = remember { mutableStateListOf<Uri>() }

    LaunchedEffect(key1 = photos) {
        tempPhotosState.apply {
            clear()
            addAll(photos)
        }
    }

    if (dialogState.value) {
        AddPhotoDialog(
            photos = tempPhotosState,
            dialogState = dialogState,
            onSavePhotosClick = { newPhotos ->
                tempPhotosState.apply {
                    clear()
                    addAll(newPhotos)
                }
                onSavePhotos(tempPhotosState)
            }
        )
    }

    Column(modifier = modifier) {
        MaxCounterView(
            modifier = Modifier
                .align(Alignment.End)
                .padding(8.dp),
            count = tempPhotosState.size,
            maxCount = Constants.MAX_PHOTOS,
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
                        LazyRow() {
                            items(items = tempPhotosState) {
                                ItemCatchPhotoView(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    photo = it
                                )
                            }
                        }
                        DefaultTextButton(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp),
                            text = stringResource(id = R.string.edit),
                            onClick = { dialogState.value = true }
                        )
                    }
                } else {
                    PrimaryTextSmall(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.photos_not_available)
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
                    PrimaryTextSmall(text = stringResource(R.string.no_photos_added))
                    ButtonWithIcon(
                        text = stringResource(id = R.string.add_photo),
                        icon = painterResource(id = R.drawable.ic_baseline_add_photo_alternate_24),
                        onClick = {
                            dialogState.value = true
                        }
                    )
                }
            }
        }
    }


}