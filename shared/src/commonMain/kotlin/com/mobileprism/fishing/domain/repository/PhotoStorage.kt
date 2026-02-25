package com.mobileprism.fishing.domain.repository

interface PhotoStorage {
    suspend fun uploadPhotos(
        photos: List<String>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)? = null
    ): List<String>
    suspend fun deletePhoto(url: String)
}
