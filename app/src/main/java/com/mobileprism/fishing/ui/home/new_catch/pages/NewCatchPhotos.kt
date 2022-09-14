package com.mobileprism.fishing.ui.home.new_catch.pages

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.catch.addPhoto
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlinedOld
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NewCatchPhotoView
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel, navController: NavController) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (subtitle, counter, button, photosView) = createRefs()

        val context = LocalContext.current
        val internetConnectionState = context.observeConnectivityAsFlow()
            .collectAsState(initial = ConnectionState.Available)

        val photos = viewModel.photos.collectAsState()
        val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
        val addPhotoState = rememberSaveable { mutableStateOf(false) }

        val choosePhotoLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { value ->
                if ((value.size + photos.value.size) > Constants.MAX_PHOTOS) {
                    SnackbarManager.showMessage(R.string.max_photos_allowed)
                }
                viewModel.addPhotos(value)
                addPhotoState.value = false
            }

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = R.drawable.ic_baseline_photo_24,
            text = stringResource(R.string.photos)
        )

        MaxCounterView(
            modifier = Modifier.constrainAs(counter) {
                top.linkTo(subtitle.top)
                bottom.linkTo(subtitle.bottom)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            count = photos.value.size,
            maxCount = Constants.MAX_PHOTOS
        )

        DefaultButtonOutlinedOld(
            modifier = Modifier.constrainAs(button) {
                bottom.linkTo(parent.bottom, 8.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            text = stringResource(id = R.string.add),
            icon = painterResource(id = R.drawable.ic_baseline_add_photo_alternate_24),
            enabled = internetConnectionState.value is ConnectionState.Available,
            onClick = { addPhotoState.value = true }
        )

        NewCatchPhotoView(
            modifier = Modifier.constrainAs(photosView) {
                top.linkTo(subtitle.bottom, 32.dp)
                bottom.linkTo(button.top, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                height = Dimension.fillToConstraints
            },
            photos = photos.value,
            onDelete = { viewModel.deletePhoto(it) }
        )

        if (addPhotoState.value) {
            LaunchedEffect(addPhotoState) {
                permissionState.launchPermissionRequest()
            }
            addPhoto(permissionState, addPhotoState, choosePhotoLauncher)
        }
    }


}