package com.joesemper.fishing.data.datasource

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.joesemper.fishing.utils.getNewPhotoId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


class CloudStorageImpl : Storage {

    private val storage = Firebase.storage
    private var storageRef = storage.reference

    @ExperimentalCoroutinesApi
    override suspend fun uploadPhoto(uri: Uri) = callbackFlow {
        val riversRef = storageRef.child("markerImages/${getNewPhotoId()}")
        val uploadTask = riversRef.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            riversRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                trySend(downloadUri.toString())
            }
        }
        awaitClose { uploadTask.cancel() }
    }

    @ExperimentalCoroutinesApi
    override suspend fun uploadPhotos(uris: List<Uri>) = callbackFlow {
        val uploadTasks = mutableListOf<UploadTask>()
        uris.forEach { photoUri ->
            val riversRef = storageRef.child("markerImages/${getNewPhotoId()}")
            val uploadTask = riversRef.putFile(photoUri)
            uploadTasks.add(uploadTask)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                riversRef.downloadUrl
            }.addOnCompleteListener { task ->
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

