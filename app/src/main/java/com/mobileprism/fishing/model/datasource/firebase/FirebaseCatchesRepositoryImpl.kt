package com.mobileprism.fishing.model.datasource.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.entity.common.CatchesContentState
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FirebaseCatchesRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val cloudPhotoStorage: PhotoStorage,
    private val context: Context
) : CatchesRepository {


    override fun getAllUserCatchesState() = channelFlow {
        val listeners = mutableListOf<Task<QuerySnapshot>>()
        listeners.add(
            dbCollections.getUserMapMarkersCollection().get()
                .addOnSuccessListener(getUserCatchesStateListener(this))
        )
        awaitClose { }
    }

    @ExperimentalCoroutinesApi
    private suspend fun getUserCatchesStateListener(scope: ProducerScope<CatchesContentState>): OnSuccessListener<in QuerySnapshot> =
        OnSuccessListener<QuerySnapshot> { task ->
            scope.launch {
                getCatchesStateFromDoc(task.documents).collect {
                    scope.trySend(it)
                }
            }
        }

    @ExperimentalCoroutinesApi
    private fun getCatchesStateFromDoc(docs: List<DocumentSnapshot>) = callbackFlow {
        docs.forEach { doc ->
            doc.reference.collection(CATCHES_COLLECTION)
                .addSnapshotListener { snapshots, error ->
                    if (snapshots != null) {

                        val result = CatchesContentState()

                        for (dc in snapshots.documentChanges) {
                            val userCatch = dc.document.toObject<UserCatch>()
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    result.added.add(userCatch)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    result.modified.add(userCatch)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    result.deleted.add(userCatch)
                                }
                            }
                        }

                        trySend(result)
                    }
                }
        }
        awaitClose { }
    }

    override fun getAllUserCatchesList() = channelFlow {
        //val snapshot = await firestore.collection('events').get()
        val listeners = mutableListOf<Task<QuerySnapshot>>()
        listeners.add(
            dbCollections.getUserMapMarkersCollection().get()
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
                val result = mutableListOf<UserCatch>()

                if (task.documents.size > 0)
                    getCatchesFromDoc(task.documents).take(task.documents.size).onCompletion {
                        scope.trySend(result)
                    }.collect {
                        result.addAll(it)
                    }
            }
        }

    override fun getCatchesByMarkerId(markerId: String) = channelFlow {
        val listener = dbCollections.getUserCatchesCollection(markerId)
            .addSnapshotListener(getCatchSnapshotListener(this))
        awaitClose {
            listener.remove()
        }
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

        }


    override fun addNewCatch(
        markerId: String,
        newCatch: UserCatch
    ) = callbackFlow {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id)
            .set(newCatch)
            .addOnCompleteListener {
                if (it.exception != null) {
                    trySend(Progress.Error(it.exception!!))
                }
                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("new_catch", null)
                    trySend(Progress.Complete)
                    incrementNumOfCatches(markerId)
                }
            }
        awaitClose { }
    }

    override fun addNewCatchOffline(markerId: String, newCatch: UserCatch) = callbackFlow {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id).set(newCatch)
        firebaseAnalytics.logEvent("new_catch", null)
        trySend(Progress.Complete)
        incrementNumOfCatches(markerId)
        awaitClose { }
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        dbCollections.getUserCatchesCollection(userCatch.userMarkerId).document(userCatch.id)
            .delete().addOnSuccessListener {
                decrementNumOfCatches(userCatch.userMarkerId)
            }
        userCatch.downloadPhotoLinks.forEach {
            cloudPhotoStorage.deletePhoto(it)
        }
    }

    override suspend fun updateUserCatch(
        markerId: String,
        catchId: String,
        data: Map<String, Any>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(catchId).update(data)
    }

    override fun subscribeOnUserCatchState(markerId: String, catchId: String) =
        channelFlow<UserCatch> {

            val listener =
                dbCollections.getUserCatchesCollection(markerId).whereEqualTo("id", catchId)
                    .addSnapshotListener { snapshots, error ->

                        for (dc in snapshots!!.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.MODIFIED -> {
                                    trySend(dc.document.toObject())
                                }
                                DocumentChange.Type.ADDED -> {
                                }
                                DocumentChange.Type.REMOVED -> {
                                }
                            }
                        }
                    }
            awaitClose {
                listener.remove()
            }
        }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<Uri>
    ): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading(0))

        val newPhotoDownloadLinks =
            savePhotos(newPhotos.filter { !it.toString().startsWith("http") })

        val oldPhotos = newPhotos.filter { it.toString().startsWith("http") }

        val photosResult = newPhotoDownloadLinks + oldPhotos.map { it.toString() }
        dbCollections.getUserCatchesCollection(markerId).document(catchId)
            .update("downloadPhotoLinks", photosResult)
            .addOnCompleteListener { flow.tryEmit(Progress.Complete) }

        return flow
    }

    private fun incrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(1))
    }

    private fun decrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(-1))
    }

    private suspend fun savePhotos(photos: List<Uri>) = cloudPhotoStorage.uploadPhotos(photos)

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_MARKERS_COLLECTION = "markers"
        private const val USER_CATCHES_COLLECTION = "catches"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }

}