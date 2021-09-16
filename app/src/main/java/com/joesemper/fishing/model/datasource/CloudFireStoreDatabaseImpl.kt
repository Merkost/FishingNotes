package com.joesemper.fishing.model.datasource

import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.mappers.MapMarkerMapper
import com.joesemper.fishing.model.mappers.UserCatchMapper
import com.joesemper.fishing.utils.getCurrentUserId
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class CloudFireStoreDatabaseImpl(private val cloudPhotoStorage: PhotoStorage) : DatabaseProvider {

    private val db = Firebase.firestore

    @ExperimentalCoroutinesApi
    override fun getAllUserCatchesList() = channelFlow {
        //val snapshot = await firestore.collection('events').get()
        val listeners = mutableListOf<Task<QuerySnapshot>>()
        listeners.add(
            getUserMapMarkersCollection().get()
                .addOnSuccessListener(getUserCatchesSuccessListener(this))
        )
        /* TODO: Get user's public markers
        listeners.add(
            getMapMarkersCollection()
                .whereEqualTo("userId", getCurrentUserId())
                .addOnCompleteListener(getUserCatchesSuccessListener(this))
        )*/
        awaitClose {
            //listeners.forEach { it.remove() }
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun getUserCatchesSuccessListener(scope: ProducerScope<List<UserCatch>>): OnSuccessListener<in QuerySnapshot> =
        OnSuccessListener<QuerySnapshot> { task ->
            scope.launch {
                val catches = mutableListOf<UserCatch>()
                task.documents.forEach { doc ->
                    async {
                        doc.reference.collection(CATCHES_COLLECTION).addSnapshotListener(
                            EventListener<QuerySnapshot> { snapshots, error ->
                                snapshots?.toObjects(UserCatch::class.java)?.forEach {
                                    catches.add(it)
                                }
                            }
                        )
                    }.await()
                }
                scope.trySend(catches)
            }
        }

    // val markers = task.toObjects(UserCatch::class.java)
    //val catches = mutableListOf<UserCatch>()
//            if (task.isSuccessful) {
//                val catches = task.result.toObjects(UserCatch::class.java)
    //scope.trySend(catches)
//                for (document in task.result) {
//                    catches.add(document.toObject(UserCatch::class.java))
//                    scope.trySend(catches)
//                    document.data
//                    Log.d(TAG, document.id + " => " + document.data)
//                }
//            } else {
//                //Log.d(TAG, "Error getting documents: ", task.exception)
//                scope.trySend(listOf())
//            }


//                Log.d("Fishing", "Catch snapshot listener", error)
//                return@EventListener
//            }
//
//            if (snapshots != null) {
//                val catches = snapshots.toObjects(UserCatch::class.java)
//                scope.trySend(catches)
//            } else {
//                scope.trySend(listOf())
//            }
//
////            for (dc in snapshots!!.documentChanges) {
////                when (dc.type) {
////                    DocumentChange.Type.ADDED -> {
////                        val userCatch = dc.document.toObject<UserCatch>()
////                        scope.trySend(userCatch)
////                    }
////                    DocumentChange.Type.MODIFIED -> {
////                    }
////                    DocumentChange.Type.REMOVED -> {
////                    }
////                }
////            }


    @ExperimentalCoroutinesApi
    override fun getCatchesByMarkerId(markerId: String) = channelFlow {
        val listener = getUserCatchesCollection(markerId)
            .addSnapshotListener(getCatchSnapshotListener(this))
        awaitClose {
            listener.remove()
        }
    }


    @ExperimentalCoroutinesApi
    override fun getAllMarkers() = channelFlow<MapMarker> {
        val listeners = mutableListOf<ListenerRegistration>()

        //UserMarkers
        listeners.add(
            getUserMapMarkersCollection().addSnapshotListener(getMarkersSnapshotListener(this))
        )
        //AllPublicMarkers
        listeners.add(
            getMapMarkersCollection()
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
            getUserMapMarkersCollection()
                .addSnapshotListener(getMarkersListSnapshotListener(this))
        )

//        listeners.add(
//            getMapMarkersCollection()
//                .whereEqualTo("isPublic", true)
//                .whereNotEqualTo("userId", getCurrentUserId())
//                .addSnapshotListener(getMarkersListSnapshotListener(this))
//        )

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }


    @ExperimentalCoroutinesApi
    override fun getMapMarker(markerId: String) = channelFlow {
        val listener = getUserMapMarkersCollection().document(markerId)
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
            snapshots?.let { snapshot ->
                for (dc in snapshot.documentChanges) {
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
    override suspend fun addNewUser(user: User): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())
        if (user.isAnonymous) {
            flow.emit(Progress.Complete)
        } else {
            getUsersCollection().document(user.userId).set(user)
                .addOnCompleteListener {
                    flow.tryEmit(Progress.Complete)
                }
        }
        return flow
    }

    @ExperimentalCoroutinesApi
    override suspend fun addNewCatch(
        markerId: String,
        newCatch: RawUserCatch
    ): StateFlow<Progress> {

        val flow = MutableStateFlow<Progress>(Progress.Loading())
        val photoDownloadLinks = savePhotos(newCatch.photos)
        val userCatch = UserCatchMapper().mapRawCatch(newCatch, photoDownloadLinks)

        getUserCatchesCollection(markerId).document(userCatch.id).set(userCatch)
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
        val documentRef = getUserMapMarkersCollection()
            .document(userMapMarker.id)
        val task = documentRef.set(userMapMarker)
        task.addOnCompleteListener {
            trySend(userMapMarker.id)
        }
        awaitClose {}
    }


    private suspend fun savePhotos(photos: List<ByteArray>) =
        cloudPhotoStorage.uploadPhotos(photos)

    override suspend fun deleteMarker(userMapMarker: UserMapMarker) {
        getUserMapMarkersCollection().document(userMapMarker.id).delete()
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        getUserCatchesCollection(userCatch.userMarkerId).document(userCatch.id).delete()
    }


    private fun getUsersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION)
    }

    private fun getUserMapMarkersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION).document(getCurrentUserId())
            .collection(MARKERS_COLLECTION)
    }

    private fun getUserCatchesCollection(usermarkerId: String): CollectionReference {
        return db.collection(USERS_COLLECTION).document(getCurrentUserId())
            .collection(MARKERS_COLLECTION).document(usermarkerId)
            .collection(CATCHES_COLLECTION)
    }

    private fun getMapMarkersCollection(): CollectionReference {
        return db.collection(MARKERS_COLLECTION)
    }

    private fun getCatchesCollection(usermarkerId: String): CollectionReference {
        return db.collection(MARKERS_COLLECTION).document(usermarkerId)
            .collection(CATCHES_COLLECTION)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_MARKERS_COLLECTION = "markers"
        private const val USER_CATCHES_COLLECTION = "catches"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }


}