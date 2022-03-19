package com.mobileprism.fishing.domain.use_cases

import android.net.Uri
import com.mobileprism.fishing.model.repository.PhotoStorage

class SavePhotosUseCase(
    private val cloudPhotoStorage: PhotoStorage
) {

    suspend operator fun invoke(photos: List<Uri>): List<String> {
        val newPhotoDownloadLinks =
            cloudPhotoStorage.uploadPhotos(photos.filter { !it.toString().startsWith("http") })
        val oldPhotos = photos.filter { it.toString().startsWith("http") }

        return newPhotoDownloadLinks + oldPhotos.map { it.toString() }
    }

}