package com.mobileprism.fishing.domain.use_cases

import android.net.Uri
import com.mobileprism.fishing.domain.repository.PhotoStorage

class SavePhotosUseCase(
    private val cloudPhotoStorage: PhotoStorage
) {

    suspend operator fun invoke(
        photos: List<Uri>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)? = null
    ): List<String> {
        val newPhotos = photos.filter { !it.toString().startsWith("http") }
        val newPhotoDownloadLinks = cloudPhotoStorage.uploadPhotos(newPhotos, onProgress)
        val oldPhotos = photos.filter { it.toString().startsWith("http") }

        return newPhotoDownloadLinks + oldPhotos.map { it.toString() }
    }

}