package com.mobileprism.fishing.domain.repository

import android.net.Uri


interface PhotoStorage {
    suspend fun uploadPhotos(
        photos: List<Uri>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)? = null
    ): List<String>
    suspend fun deletePhoto(url: String)
}