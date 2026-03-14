package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.repository.PhotoStorage

class IosPhotoStorage : PhotoStorage {
    override suspend fun uploadPhotos(
        photos: List<String>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)?
    ): Result<List<String>> = Result.success(emptyList())

    override suspend fun deletePhoto(url: String): Result<Unit> = Result.success(Unit)
}
