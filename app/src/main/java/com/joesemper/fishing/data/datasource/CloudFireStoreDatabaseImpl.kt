package com.joesemper.fishing.data.datasource

import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.data.entity.raw.RawUserCatch
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.mappers.MapMarkerMapper
import com.joesemper.fishing.data.mappers.UserCatchMapper
import com.joesemper.fishing.data.entity.content.UserCatch
import com.joesemper.fishing.data.entity.content.UserMapMarker
import com.joesemper.fishing.data.entity.common.Progress
import com.joesemper.fishing.data.entity.content.MapMarker
import com.joesemper.fishing.utils.getCurrentUser
import com.joesemper.fishing.utils.getCurrentUserId
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class CloudFireStoreDatabaseImpl(private val cloudPhotoStorage: PhotoStorage) : DatabaseProvider {

    private val db = Firebase.firestore

    @ExperimentalCoroutinesApi
    override fun getAllUserCatches() = channelFlow {
        val listeners = mutableListOf<ListenerRegistration>()
        listeners.add(
            getCatchesCollection()
                .whereEqualTo("userId", getCurrentUser()!!.uid)
                .addSnapshotListener(getCatchSnapshotListener(this))
        )
        listeners.add(
            getCatchesCollection()
                .whereEqualTo("isPublic", true)
                .whereNotEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getCatchSnapshotListener(this))
        )
        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    override fun getCatchesByMarkerId(markerId: String) = channelFlow {
        val listener = getCatchesCollection()
            .whereEqualTo("userMarkerId", markerId)
            .addSnapshotListener(getCatchSnapshotListener(this))
        awaitClose {
            listener.remove()
        }
    }

    @ExperimentalCoroutinesApi
    override fun getAllMarkers() = channelFlow<MapMarker> {
        val listeners = mutableListOf<ListenerRegistration>()
        listeners.add(
            getMapMarkersCollection()
                .whereEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersSnapshotListener(this))
        )
        listeners.add(
            getMapMarkersCollection()
                .whereEqualTo("isPublic", true)
                .whereNotEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersSnapshotListener(this))
        )

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    override fun getAllUserMarkersList() = channelFlow {
        val listeners = mutableListOf<ListenerRegistration>()
        listeners.add(
            getMapMarkersCollection()
                .whereEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersListSnapshotListener(this))
        )
        listeners.add(
            getMapMarkersCollection()
                .whereEqualTo("isPublic", true)
                .whereNotEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersListSnapshotListener(this))
        )

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    override fun getMapMarker(markerId: String) = channelFlow {
        val listener = getMapMarkersCollection().document(markerId)
            .addSnapshotListener { value, error ->
                trySend(value?.toObject<UserMapMarker>())
            }
        awaitClose { listener.remove() }
    }

    @ExperimentalCoroutinesApi
    private fun getCatchSnapshotListener(scope: ProducerScope<List<UserCatch>>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Catch snapshot listener", error)
                return@EventListener
            }

            if (snapshots != null) {
                val catches = snapshots.toObjects(UserCatch::class.java)
                scope.trySend(catches)
            } else {
                scope.trySend(listOf())
            }

//            for (dc in snapshots!!.documentChanges) {
//                when (dc.type) {
//                    DocumentChange.Type.ADDED -> {
//                        val userCatch = dc.document.toObject<UserCatch>()
//                        scope.trySend(userCatch)
//                    }
//                    DocumentChange.Type.MODIFIED -> {
//                    }
//                    DocumentChange.Type.REMOVED -> {
//                    }
//                }
//            }
        }

    @ExperimentalCoroutinesApi
    private fun getMarkersSnapshotListener(scope: ProducerScope<UserMapMarker>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val mapMarker = dc.document.toObject<UserMapMarker>()
                        scope.trySend(mapMarker)
                    }
                    DocumentChange.Type.MODIFIED -> {
                    }
                    DocumentChange.Type.REMOVED -> {
                    }
                }
            }
        }

    @ExperimentalCoroutinesApi
    private fun getMarkersListSnapshotListener(scope: ProducerScope<List<UserMapMarker>>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }

            if (snapshots != null) {
                val markers = snapshots.toObjects(UserMapMarker::class.java)
                scope.trySend(markers)
            }
        }

    @ExperimentalCoroutinesApi
    override suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())

        val photoDownloadLinks = savePhotos(newCatch.photos)

        val userCatch = UserCatchMapper().mapRawCatch(newCatch, photoDownloadLinks)

        getCatchesCollection().document(userCatch.id).set(userCatch)
            .addOnCompleteListener {
                flow.tryEmit(Progress.Complete)
            }
        return flow
    }

    @ExperimentalCoroutinesApi
    override suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())
        val mapMarker = MapMarkerMapper().mapRawMapMarker(newMarker)
        try {
            saveMarker(mapMarker)
        } catch (error: Throwable) {
            flow.emit(Progress.Error(error))
        }
        flow.emit(Progress.Complete)
        return flow
    }

    @ExperimentalCoroutinesApi
    private suspend fun saveMarker(userMapMarker: UserMapMarker?): String {
        var markerId = ""
        if (userMapMarker != null) {
            saveMarkerToDb(userMapMarker)
                .take(1)
                .collect {
                    markerId = it
                }
        }
        return markerId
    }

    @ExperimentalCoroutinesApi
    private fun saveMarkerToDb(userMapMarker: UserMapMarker) = callbackFlow {
        val documentRef = getMapMarkersCollection().document(userMapMarker.id)
        val task = documentRef.set(userMapMarker)
        task.addOnCompleteListener {
            trySend(userMapMarker.id)
        }
        awaitClose {}
    }


    private suspend fun savePhotos(photos: List<ByteArray>) =
        cloudPhotoStorage.uploadPhotos(photos)

    override suspend fun deleteMarker(userCatch: UserCatch) {
//        cloudStorage.deletePhoto(userCatch.downloadPhotoLink)
//        getUserMarkersCollection().document(userCatch.id).delete()
    }

    private fun getMapMarkersCollection(): CollectionReference {
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