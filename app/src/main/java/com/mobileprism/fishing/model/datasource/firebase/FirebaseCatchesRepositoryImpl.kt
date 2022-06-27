package com.mobileprism.fishing.model.datasource.firebase

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.tasks.await

class FirebaseCatchesRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val connectionManager: ConnectionManager
) : CatchesRepository {


    override fun getAllUserCatchesState() = channelFlow<ContentStateOld<UserCatch>> {
        val listeners = mutableListOf<Task<QuerySnapshot>>()
        listeners.add(
            dbCollections.getUserMapMarkersCollection().get()
                .addOnSuccessListener(getUserCatchesStateListener(this))
        )
        awaitClose { }
    }

    @ExperimentalCoroutinesApi
    private suspend fun getUserCatchesStateListener(scope: ProducerScope<ContentStateOld<UserCatch>>)
            : OnSuccessListener<in QuerySnapshot> =
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
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {

                        val result = ContentStateOld<UserCatch>()

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

    override fun getCatchById(markerId: String, catchId: String): Flow<UserCatch?> = callbackFlow {
        dbCollections.getUserCatchesCollection(markerId).document(catchId)
            .addSnapshotListener { value, error ->
                trySend(value?.toObject<UserCatch>())
            }
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

    override suspend fun getCatchesByMarkerId(markerId: String) =
        suspendCoroutine<Result<List<UserCatch>>> { continuation ->
            dbCollections.getUserCatchesCollection(markerId).get()
                .addOnFailureListener {
                    Log.d("Fishing", "Catch snapshot listener", it)
                    continuation.resumeWith(Result.failure(it))
                }
                .addOnSuccessListener {
                    continuation.resume(Result.success(it.toObjects(UserCatch::class.java)))
                }
        }

        }


    override fun addNewCatch(
        markerId: String,
        newCatch: UserCatch
    ): Flow<Result<Unit>> = channelFlow {
        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        Result.success(
            if (isOnline) {
                addNewCatchOnline(markerId = markerId, newCatch = newCatch, scope = this)
            } else {
                addNewCatchOffline(markerId = markerId, newCatch = newCatch, scope = this)
            }
        )

        awaitClose { }
    }

    private fun addNewCatchOnline(
        markerId: String,
        newCatch: UserCatch,
        scope: ProducerScope<Result<Unit>>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id)
            .set(newCatch)
            .addOnCompleteListener {
                if (it.exception != null) {
                    scope.trySend(Result.failure(it.exception!!))
                }
                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("new_catch", null)
                    scope.trySend(Result.success(Unit))
                    incrementNumOfCatches(markerId)
                }
            }
    }

    private fun addNewCatchOffline(
        markerId: String,
        newCatch: UserCatch,
        scope: ProducerScope<Result<Unit>>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id).set(newCatch)
        firebaseAnalytics.logEvent("new_catch_offline", null)
        scope.trySend(Result.success(Unit))
        incrementNumOfCatches(markerId)
    }


    override suspend fun deleteCatch(userCatch: UserCatch) {
        dbCollections.getUserCatchesCollection(userCatch.markerId).document(userCatch.id)
            .delete().addOnSuccessListener {
                decrementNumOfCatches(userCatch.markerId)
            }
    }

    override suspend fun updateUserCatch(
        userCatch: UserCatch,
    ) {
        dbCollections.getUserCatchesCollection(userCatch.markerId).document(userCatch.id)
            .set(userCatch)
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<Uri>
    ): StateFlow<Progress> {
        TODO("Not yet implemented")
    }

    override fun subscribeOnUserCatchState(markerId: String, catchId: String) =
        channelFlow<UserCatch> {

            val listener =
                dbCollections.getUserCatchesCollection(markerId).whereEqualTo("id", catchId)
                    .addSnapshotListener { snapshots, _ ->

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

//    override suspend fun updateUserCatchPhotos(
//        markerId: String,
//        catchId: String,
//        newPhotos: List<Uri>
//    ): StateFlow<Progress> {
//        val flow = MutableStateFlow<Progress>(Progress.Loading(0))
//
//        val newPhotoDownloadLinks =
//            savePhotos(newPhotos.filter { !it.toString().startsWith("http") })
//
//        val oldPhotos = newPhotos.filter { it.toString().startsWith("http") }
//
//        val photosResult = newPhotoDownloadLinks + oldPhotos.map { it.toString() }
//        dbCollections.getUserCatchesCollection(markerId).document(catchId)
//            .update("downloadPhotoLinks", photosResult)
//            .addOnCompleteListener { flow.tryEmit(Progress.Complete) }
//
//        return flow
//    }

    private fun incrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(1))
    }

    private fun decrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(-1))
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_MARKERS_COLLECTION = "markers"
        private const val USER_CATCHES_COLLECTION = "catches"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }

}