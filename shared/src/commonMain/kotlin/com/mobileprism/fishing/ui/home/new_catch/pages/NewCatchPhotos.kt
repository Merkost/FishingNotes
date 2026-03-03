package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NewCatchPhotoView
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.utils.rememberMediaPickerLauncher
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.rememberConnectionState

@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val internetConnectionState by rememberConnectionState()

        val photos = viewModel.photos.collectAsState()

        val mediaPicker = rememberMediaPickerLauncher(
            maxPhotos = Constants.MAX_PHOTOS,
            onResult = { newPhotos ->
                if ((newPhotos.size + photos.value.size) > Constants.MAX_PHOTOS) {
                    SnackbarManager.showMessage(Res.string.max_photos_allowed)
                }
                viewModel.addPhotos(newPhotos)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubtitleWithIcon(
                icon = Res.drawable.ic_baseline_photo_24,
                text = stringResource(Res.string.photos)
            )

            Spacer(modifier = Modifier.weight(1f))

            MaxCounterView(
                count = photos.value.size,
                maxCount = Constants.MAX_PHOTOS
            )
        }

        NewCatchPhotoView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 32.dp, bottom = 8.dp),
            photos = photos.value,
            onDelete = { viewModel.deletePhoto(it) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            DefaultButtonOutlined(
                text = stringResource(Res.string.gallery),
                icon = painterResource(Res.drawable.ic_baseline_add_photo_alternate_24),
                enabled = internetConnectionState is ConnectionState.Available,
                onClick = { mediaPicker.launchGallery() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultButtonOutlined(
                text = stringResource(Res.string.camera),
                icon = painterResource(Res.drawable.ic_baseline_photo_camera_24),
                onClick = { mediaPicker.launchCamera() }
            )
        }
    }
}
