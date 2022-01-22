package com.joesemper.fishing.model.repository

import android.net.Uri


interface PhotoStorage {
    suspend fun uploadPhotos(photos: List<Uri>): List<String>
    suspend fun deletePhoto(url: String)
}