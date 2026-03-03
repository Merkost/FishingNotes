package com.mobileprism.fishing.ui.utils

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberMediaPickerLauncher(
    maxPhotos: Int,
    onResult: (List<String>) -> Unit,
): MediaPickerLauncher {
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxPhotos)
    ) { uris ->
        onResult(uris.map { it.toString() })
    }

    val cameraPhotoUri = remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraPhotoUri.value?.let { uri ->
                onResult(listOf(uri.toString()))
            }
        }
    }

    return remember(pickMedia, takePicture) {
        MediaPickerLauncher(
            pickMediaLauncher = pickMedia,
            takePictureLauncher = takePicture,
            cameraPhotoUri = cameraPhotoUri,
            createTempUri = {
                val photoFile = File.createTempFile("catch_photo_", ".jpg", context.cacheDir)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
            }
        )
    }
}

actual class MediaPickerLauncher(
    private val pickMediaLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    private val takePictureLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    private val cameraPhotoUri: androidx.compose.runtime.MutableState<Uri?>,
    private val createTempUri: () -> Uri,
) {
    actual fun launchGallery() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    actual fun launchCamera() {
        val uri = createTempUri()
        cameraPhotoUri.value = uri
        takePictureLauncher.launch(uri)
    }
}
