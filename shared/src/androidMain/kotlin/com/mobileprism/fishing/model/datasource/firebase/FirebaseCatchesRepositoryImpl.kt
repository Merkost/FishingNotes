package com.mobileprism.fishing.model.datasource.firebase

import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.*
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.utils.getCurrentUserId
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class FirebaseCatchesRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker,
    private val connectionManager: ConnectionManager
) : CatchesRepository {

    override fun getAllUserCatchesState() = callbackFlow<ContentStateOld<UserCatch>> {
        val userId = getCurrentUserId()
        val listener = dbCollections.db.collectionGroup(CATCHES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Fishing", "Collection group snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val result = ContentStateOld<UserCatch>()
                    for (dc in snapshots.documentChanges) {
                        val userCatch = dc.document.toObject(UserCatch::class.java)
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> result.added.add(userCatch)
                            DocumentChange.Type.MODIFIED -> result.modified.add(userCatch)
                            DocumentChange.Type.REMOVED -> result.deleted.add(userCatch)
                        }
                    }
                    trySend(result)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getAllUserCatchesList() = callbackFlow {
        val userId = getCurrentUserId()
        val listener = dbCollections.db.collectionGroup(CATCHES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Fishing", "Collection group list snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val catches = snapshots.toObjects(UserCatch::class.java)
                    trySend(catches)
                }
            }
        awaitClose { listener.remove() }
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
                Log.e("Fishing", "Catch snapshot listener", error)
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
    ) = channelFlow {
        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available

        if (isOnline) {
            addNewCatchOnline(markerId = markerId, newCatch = newCatch, scope = this)
        } else {
            addNewCatchOffline(markerId = markerId, newCatch = newCatch, scope = this)
        }

        awaitClose { }
    }

    private fun addNewCatchOnline(
        markerId: String,
        newCatch: UserCatch,
        scope: ProducerScope<Result<Nothing?>>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id)
            .set(newCatch)
            .addOnCompleteListener {
                it.exception?.let { ex ->
                    scope.trySend(Result.failure(ex))
                }
                if (it.isSuccessful) {
                    analyticsTracker.logEvent(AnalyticsEvent.NewCatch)
                    scope.trySend(Result.success(null))
                    incrementNumOfCatches(markerId)
                }
            }
    }

    private fun addNewCatchOffline(
        markerId: String,
        newCatch: UserCatch,
        scope: ProducerScope<Result<Nothing?>>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id).set(newCatch)
        analyticsTracker.logEvent(AnalyticsEvent.NewCatchOffline)
        scope.trySend(Result.success(null))
        incrementNumOfCatches(markerId)
    }


    override suspend fun deleteCatch(userCatch: UserCatch) {
        dbCollections.getUserCatchesCollection(userCatch.userMarkerId).document(userCatch.id)
            .delete().addOnSuccessListener {
                decrementNumOfCatches(userCatch.userMarkerId)
            }
    }

    override suspend fun updateUserCatch(
        markerId: String,
        catchId: String,
        data: Map<String, Any>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(catchId).update(data)
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<Uri>
    ): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading(0))

        val photosResult = newPhotos.map { it.toString() }
        dbCollections.getUserCatchesCollection(markerId).document(catchId)
            .update("downloadPhotoLinks", photosResult)
            .addOnCompleteListener { flow.tryEmit(Progress.Complete) }

        return flow
    }

    override fun subscribeOnUserCatchState(markerId: String, catchId: String) =
        channelFlow<UserCatch> {

            val listener =
                dbCollections.getUserCatchesCollection(markerId).whereEqualTo("id", catchId)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) {
                            Log.e("Fishing", "Catch state snapshot listener", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener

                        for (dc in snapshots.documentChanges) {
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

    private fun incrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(1))
    }

    private fun decrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(-1))
    }

    override fun getAllUserCatchesPaged(
        sortField: String,
        sortDirection: Query.Direction
    ): Flow<PagingData<UserCatch>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            CatchesPagingSource(dbCollections.db, getCurrentUserId(), sortField, sortDirection)
        }
    ).flow

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_MARKERS_COLLECTION = "markers"
        private const val USER_CATCHES_COLLECTION = "catches"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }

}
