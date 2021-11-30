package com.joesemper.fishing.model.datasource

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.joesemper.fishing.model.entity.common.CatchesContentState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.mappers.UserCatchMapper
import com.joesemper.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.joesemper.fishing.model.datasource.RepositoryCollections.*
import org.koin.androidx.compose.get
import kotlin.collections.forEach

class FirebaseCatchesRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val cloudPhotoStorage: PhotoStorage
) : CatchesRepository {


    @ExperimentalCoroutinesApi
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
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val userCatch = dc.document.toObject<UserCatch>()
                                    result.added.add(userCatch)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val userCatch = dc.document.toObject<UserCatch>()
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

    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
    private fun getCatchesFromDoc(docs: List<DocumentSnapshot>) = callbackFlow {
        docs.forEach { doc ->
            doc.reference.collection(CATCHES_COLLECTION)
                .addSnapshotListener { snapshots, error ->
                    if (snapshots != null) {
                        val catches = snapshots.toObjects(UserCatch::class.java)
                        trySend(catches)
                    } else {
                        trySend(listOf<UserCatch>())
                    }

                }
        }
        awaitClose { }
    }

    @ExperimentalCoroutinesApi
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


    @ExperimentalCoroutinesApi
    override suspend fun addNewCatch(
        markerId: String,
        newCatch: RawUserCatch
    ): StateFlow<Progress> {

        val flow = MutableStateFlow<Progress>(Progress.Loading(0))
        val photoDownloadLinks = savePhotos(newCatch.photos, flow)
        val userCatch = UserCatchMapper().mapRawCatch(newCatch, photoDownloadLinks)

        dbCollections.getUserCatchesCollection(markerId).document(userCatch.id).set(userCatch)
            .addOnCompleteListener {
                flow.tryEmit(Progress.Complete)
            }
        return flow
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        dbCollections.getUserCatchesCollection(userCatch.userMarkerId).document(userCatch.id)
            .delete()
        userCatch.downloadPhotoLinks.forEach {
            cloudPhotoStorage.deletePhoto(it)
        }
    }

    private suspend fun savePhotos(
        photos: List<Uri>,
        progressFlow: MutableStateFlow<Progress>
    ) =
        cloudPhotoStorage.uploadPhotos(photos, progressFlow)

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_MARKERS_COLLECTION = "markers"
        private const val USER_CATCHES_COLLECTION = "catches"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }

}