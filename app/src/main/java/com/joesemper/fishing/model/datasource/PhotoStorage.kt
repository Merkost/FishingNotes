package com.joesemper.fishing.model.datasource

import com.joesemper.fishing.model.entity.common.Progress
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File


interface PhotoStorage {
    suspend fun uploadPhotos(
        photos: List<File>,
        progressFlow: MutableStateFlow<Progress>
    ): List<String>

    suspend fun deletePhoto(url: String)
}