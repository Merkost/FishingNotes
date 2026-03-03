package com.mobileprism.fishing.model.datasource.firebase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.storage.UploadTask
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.google.firebase.storage.storage
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.utils.getNewPhotoId
import fileFromContentUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.take
import java.io.File


class FirebaseCloudPhotoStorage(
    private val analyticsTracker: AnalyticsTracker,
    private val context: Context) : PhotoStorage {

    private val storage = Firebase.storage
    private var storageRef = storage.reference

    override suspend fun uploadPhotos(
        photos: List<String>,
        onProgress: ((uploaded: Int, total: Int) -> Unit)?
    ): Result<List<String>> = try {
        val downloadLinks = mutableListOf<String>()
        if (photos.isNotEmpty()) {
            val uris = photos.map { Uri.parse(it) }
            var uploaded = 0
            savePhotosToDb(uris, context)
                .take(photos.size)
                .collect { downloadLink ->
                    downloadLinks.add(downloadLink)
                    uploaded++
                    onProgress?.invoke(uploaded, photos.size)
                }
        }

        analyticsTracker.logEvent(AnalyticsEvent.UploadPhotos(downloadLinks.size))

        Result.success(downloadLinks)
    } catch (e: Exception) {
        Result.failure(e)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun savePhotosToDb(images: List<Uri>, context: Context) = callbackFlow {
        val uploadTasks = mutableListOf<UploadTask>()

        images.forEach { uri ->
            val riversRef = storageRef.child("markerImages/${getNewPhotoId()}")

            val realFile: File = fileFromContentUri(context, uri)
            try {

                val compressedImageFile = Compressor.compress(context, realFile) {
                    quality(10)
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
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
        awaitClose { uploadTasks.onEach { cancel() } }
    }

    override suspend fun deletePhoto(url: String): Result<Unit> = try {
        val desertRef = storage.getReferenceFromUrl(url)
        desertRef.delete()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
