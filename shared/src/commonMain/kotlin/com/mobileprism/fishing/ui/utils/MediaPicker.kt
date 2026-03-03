package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberMediaPickerLauncher(
    maxPhotos: Int,
    onResult: (List<String>) -> Unit,
): MediaPickerLauncher

expect class MediaPickerLauncher {
    fun launchGallery()
    fun launchCamera()
}
