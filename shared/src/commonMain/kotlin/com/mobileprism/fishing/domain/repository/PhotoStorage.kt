package com.mobileprism.fishing.domain.repository

interface PhotoStorage {
    suspend fun uploadPhotos(
        photos: List<String>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)? = null
    ): Result<List<String>>
    suspend fun deletePhoto(url: String): Result<Unit>
}
