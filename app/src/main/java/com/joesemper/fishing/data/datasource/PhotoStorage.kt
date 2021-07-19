package com.joesemper.fishing.data.datasource


interface PhotoStorage {
    suspend fun uploadPhotos(photos: List<ByteArray>): List<String>
    suspend fun deletePhoto(url: String)
}