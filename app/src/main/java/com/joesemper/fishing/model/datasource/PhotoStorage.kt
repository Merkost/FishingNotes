package com.joesemper.fishing.model.datasource

import com.joesemper.fishing.model.entity.common.Progress
import kotlinx.coroutines.flow.MutableStateFlow


interface PhotoStorage {
    suspend fun uploadPhotos(
        photos: List<ByteArray>,
        progressFlow: MutableStateFlow<Progress>
    ): List<String>

    suspend fun deletePhoto(url: String)
}