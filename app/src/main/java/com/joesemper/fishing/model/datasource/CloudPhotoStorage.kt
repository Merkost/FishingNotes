package com.joesemper.fishing.model.datasource

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.utils.getNewPhotoId
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import java.io.File.createTempFile

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
    private fun savePhotosToDb(images: List<Uri>, context: Context) = callbackFlow {
        val uploadTasks = mutableListOf<UploadTask>()

        images.forEach { uri ->
            val riversRef = storageRef.child("markerImages/${getNewPhotoId()}")

            val stream = context.contentResolver.openInputStream(uri)

            stream?.let {
                val file = createTempFile("123", "123")
                file.writeBytes(it.readBytes())

                val compressedImageFile = Compressor.compress(context, file) {
                    quality(40)
                    format(Bitmap.CompressFormat.JPEG)
                }

                val uploadTask = riversRef.putFile(compressedImageFile.toUri())
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

                file.delete()
            }
        }
        awaitClose { uploadTasks.onEach { cancel() } }
    }

    override suspend fun deletePhoto(url: String) {
        val desertRef = storage.getReferenceFromUrl(url)
        desertRef.delete()
    }
}
