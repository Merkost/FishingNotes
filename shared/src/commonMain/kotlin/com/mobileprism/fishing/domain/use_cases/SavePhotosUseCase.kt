package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.PhotoStorage

class SavePhotosUseCase(
    private val cloudPhotoStorage: PhotoStorage
) {

    suspend operator fun invoke(
        photos: List<String>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)? = null
    ): List<String> {
        val newPhotos = photos.filter { !it.startsWith("http") }
        val newPhotoDownloadLinks = cloudPhotoStorage.uploadPhotos(newPhotos, onProgress)
        val oldPhotos = photos.filter { it.startsWith("http") }

        return newPhotoDownloadLinks + oldPhotos
    }

}
