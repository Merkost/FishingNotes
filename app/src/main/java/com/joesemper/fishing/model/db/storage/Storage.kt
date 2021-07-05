package com.joesemper.fishing.model.db.storage

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface Storage {
    suspend fun uploadPhoto(uri: Uri): Flow<String>
}