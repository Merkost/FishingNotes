package com.joesemper.fishing.model.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import java.io.ByteArrayOutputStream

class CloudPhotoStorage(private val context: Context) : PhotoStorage {

    private val storage = Firebase.storage
    private var storageRef = storage.reference

    @ExperimentalCoroutinesApi
    override suspend fun uploadPhotos(
        photos: List<Uri>,
        progressFlow: MutableStateFlow<Progress>
    ): List<String> {
        val downloadLinks = mutableListOf<String>()
        if (photos.isNotEmpty()) {
            val progressPoint = 100 / photos.size
            savePhotosToDb(photos, context)
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
    private fun savePhotosToDb(photoByteArrays: List<Uri>, context: Context) = callbackFlow {
        val uploadTasks = mutableListOf<UploadTask>()

        photoByteArrays.forEach { uri ->
            val riversRef = storageRef.child("markerImages/${getNewPhotoId()}")

            val reducedImage = compressImage(uri)

            val uploadTask = riversRef.putBytes(reducedImage)
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

    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)

        return byteArrayOutputStream.toByteArray()
    }

}

