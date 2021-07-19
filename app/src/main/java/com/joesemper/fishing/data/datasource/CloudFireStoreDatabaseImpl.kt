package com.joesemper.fishing.data.datasource

import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.data.mappers.UserCatchMapper
import com.joesemper.fishing.model.common.content.UserCatch
import com.joesemper.fishing.model.common.content.MapMarker
import com.joesemper.fishing.model.common.Progress
import com.joesemper.fishing.utils.getCurrentUser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class CloudFireStoreDatabaseImpl(private val cloudPhotoStorage: PhotoStorage) : DatabaseProvider {

    private val db = Firebase.firestore

    @ExperimentalCoroutinesApi
    override fun getAllUserCatches() = channelFlow {
        val listenerOne = getCatchesCollection()
            .whereEqualTo("userId", getCurrentUser()!!.uid)
            .addSnapshotListener { values, error ->
                if (error != null) {
                    Log.d("Fishing", "Catch snapshot listener", error)
                    return@addSnapshotListener
                }

                values?.forEach { documentSnapshot ->
                    val userCatch = documentSnapshot.toObject<UserCatch>()
                    trySend(userCatch)
                }
            }


        val listenerTwo = getCatchesCollection()
            .whereEqualTo("isPublic", true)
            .whereNotEqualTo("userId", getCurrentUser()!!.uid)
            .addSnapshotListener { values, error ->

                if (error != null) {
                    Log.d("Fishing", "Catch snapshot listener", error)
                    return@addSnapshotListener
                }

                values?.forEach { documentSnapshot ->
                    val userCatch = documentSnapshot.toObject<UserCatch>()
                    trySend(userCatch)
                }

            }

        awaitClose {

        }

    }

    @ExperimentalCoroutinesApi
    override fun getMarker(markerId: String) = channelFlow {

        val listener = getMarkersCollection().document(markerId)
            .addSnapshotListener { value, error ->
                trySend(value?.toObject<MapMarker>())
            }

        awaitClose { listener.remove() }
    }

    @ExperimentalCoroutinesApi
    private fun getCatchSnapshotListener(scope: ProducerScope<UserCatch>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Catch snapshot listener", error)
                return@EventListener
            }

            snapshots?.forEach { documentSnapshot ->
                val userCatch = documentSnapshot.toObject<UserCatch>()
                scope.trySend(userCatch)
            }
        }

    @ExperimentalCoroutinesApi
    private fun getMarkersSnapshotListener(scope: ProducerScope<MapMarker>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }

            snapshots?.forEach { documentSnapshot ->
                val userMarker = documentSnapshot.toObject<MapMarker>()
                scope.trySend(userMarker)
            }
        }

    @ExperimentalCoroutinesApi
    override suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())

        saveMarker(newCatch.marker)

        val photoDownloadLinks = savePhotos(newCatch.photos)

        val userCatch = UserCatchMapper().mapRawCatch(newCatch, photoDownloadLinks)

        getCatchesCollection().document(userCatch.id).set(userCatch)
            .addOnCompleteListener {
                flow.tryEmit(Progress.Complete)
            }

        return flow
    }

    @ExperimentalCoroutinesApi
    private suspend fun saveMarker(mapMarker: MapMarker?): String {
        var markerId = ""
        if (mapMarker != null) {
            saveMarkerToDb(mapMarker)
                .take(1)
                .collect {
                    markerId = it
                }
        }
        return markerId
    }

    @ExperimentalCoroutinesApi
    private fun saveMarkerToDb(mapMarker: MapMarker) = callbackFlow {
        val documentRef = getMarkersCollection().document(mapMarker.id)
        documentRef.set(mapMarker).addOnCompleteListener {
            trySend(mapMarker.id)
        }
        awaitClose {}
    }


    private suspend fun savePhotos(photos: List<ByteArray>) =
        cloudPhotoStorage.uploadPhotos(photos)

    override suspend fun deleteMarker(userCatch: UserCatch) {
//        cloudStorage.deletePhoto(userCatch.downloadPhotoLink)
//        getUserMarkersCollection().document(userCatch.id).delete()
    }

    private fun getMarkersCollection(): CollectionReference {
        return db.collection(MARKERS_COLLECTION)
    }

    private fun getCatchesCollection(): CollectionReference {
        return db.collection(CATCHES_COLLECTION)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }


}