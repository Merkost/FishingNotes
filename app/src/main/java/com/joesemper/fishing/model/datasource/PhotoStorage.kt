package com.joesemper.fishing.model.datasource

import android.net.Uri
import com.joesemper.fishing.model.entity.common.Progress
import kotlinx.coroutines.flow.MutableStateFlow


interface PhotoStorage {
    suspend fun uploadPhotos(
        photos: List<Uri>,
        progressFlow: MutableStateFlow<Progress>
    ): List<String>

    suspend fun deletePhoto(url: String)
}