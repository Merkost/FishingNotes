package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable

@Composable
actual fun rememberMediaPickerLauncher(
    maxPhotos: Int,
    onResult: (List<String>) -> Unit,
): MediaPickerLauncher = MediaPickerLauncher()

actual class MediaPickerLauncher {
    actual fun launchGallery() { }
    actual fun launchCamera() { }
}
