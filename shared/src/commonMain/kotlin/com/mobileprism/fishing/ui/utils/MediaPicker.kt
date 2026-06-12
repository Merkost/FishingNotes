package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.PhotoResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path

@Composable
fun rememberMediaPickerLauncher(
    maxPhotos: Int,
    onResult: (List<String>) -> Unit,
): MediaPickerLauncher {
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(maxItems = maxPhotos),
        onResult = { files ->
            onResult(files.orEmpty().map { it.path })
        }
    )

    val cameraLauncher = rememberCameraPickerLauncher(
        onResult = { file ->
            file?.let { onResult(listOf(it.path)) }
        }
    )

    return remember(galleryLauncher, cameraLauncher) {
        MediaPickerLauncher(
            galleryLauncher = galleryLauncher,
            cameraLauncher = cameraLauncher
        )
    }
}

class MediaPickerLauncher internal constructor(
    private val galleryLauncher: PickerResultLauncher,
    private val cameraLauncher: PhotoResultLauncher,
) {
    fun launchGallery() {
        galleryLauncher.launch()
    }

    fun launchCamera() {
        cameraLauncher.launch()
    }
}
