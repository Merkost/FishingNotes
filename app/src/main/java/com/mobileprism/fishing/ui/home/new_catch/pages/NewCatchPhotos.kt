package com.mobileprism.fishing.ui.home.new_catch.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.FileProvider
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NewCatchPhotoView
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.observeConnectivityAsFlow
import java.io.File

@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (subtitle, counter, buttons, photosView) = createRefs()

        val context = LocalContext.current
        val internetConnectionState = context.observeConnectivityAsFlow()
            .collectAsState(initial = ConnectionState.Available)

        val photos = viewModel.photos.collectAsState()

        val pickMedia =
            rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { value ->
                if ((value.size + photos.value.size) > Constants.MAX_PHOTOS) {
                    SnackbarManager.showMessage(R.string.max_photos_allowed)
                }
                viewModel.addPhotos(value)
            }

        val cameraPhotoUri = remember { mutableStateOf<Uri?>(null) }

        val takePicture =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    cameraPhotoUri.value?.let { uri ->
                        if ((1 + photos.value.size) > Constants.MAX_PHOTOS) {
                            SnackbarManager.showMessage(R.string.max_photos_allowed)
                        } else {
                            viewModel.addPhotos(listOf(uri))
                        }
                    }
                }
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

        Row(
            modifier = Modifier.constrainAs(buttons) {
                bottom.linkTo(parent.bottom, 8.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            horizontalArrangement = Arrangement.Center
        ) {
            DefaultButtonOutlined(
                text = stringResource(id = R.string.gallery),
                icon = painterResource(id = R.drawable.ic_baseline_add_photo_alternate_24),
                enabled = internetConnectionState.value is ConnectionState.Available,
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonOutlined(
                text = stringResource(id = R.string.camera),
                icon = painterResource(id = R.drawable.ic_baseline_photo_camera_24),
                enabled = internetConnectionState.value is ConnectionState.Available,
                onClick = {
                    val photoFile = File.createTempFile("catch_photo_", ".jpg", context.cacheDir)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    cameraPhotoUri.value = uri
                    takePicture.launch(uri)
                }
            )
        }

        NewCatchPhotoView(
            modifier = Modifier.constrainAs(photosView) {
                top.linkTo(subtitle.bottom, 32.dp)
                bottom.linkTo(buttons.top, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                height = Dimension.fillToConstraints
            },
            photos = photos.value,
            onDelete = { viewModel.deletePhoto(it) }
        )
    }
}
