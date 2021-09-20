package com.joesemper.fishing.model.datasource

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.utils.getNewPhotoId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take

class CloudPhotoStorageImpl : PhotoStorage {

    private val storage = Firebase.storage
    private var storageRef = storage.reference

    @ExperimentalCoroutinesApi
    override suspend fun uploadPhotos(
        photos: List<ByteArray>,
        progressFlow: MutableStateFlow<Progress>
    ): List<String> {
        val downloadLinks = mutableListOf<String>()
        if (photos.isNotEmpty()) {
            val progressPoint = 100 / photos.size
            savePhotosToDb(photos)
                .take(photos.size)
                .collect { downloadLink ->
                    downloadLinks.add(downloadLink)
                    val currentProgress = progressFlow.value as Progress.Loading
                    progressFlow.value = Progress.Loading(currentProgress.percents + progressPoint)
                }
        }
        return downloadLinks
    }

    @ExperimentalCoroutinesApi
    private fun savePhotosToDb(photoByteArrays: List<ByteArray>) = callbackFlow {
        val uploadTasks = mutableListOf<UploadTask>()

        photoByteArrays.forEach { photoByteArray ->
            val riversRef = storageRef.child("markerImages/${getNewPhotoId()}")

            val uploadTask = riversRef.putBytes(photoByteArray)
            uploadTasks.add(uploadTask)

            val callback = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                riversRef.downloadUrl
            }

            callback.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    trySend(downloadUri.toString())
                }
            }
        }

        awaitClose { uploadTasks.onEach { cancel() } }
    }

    override suspend fun deletePhoto(url: String) {
        val desertRef = storage.getReferenceFromUrl(url)
        desertRef.delete()
    }


}

