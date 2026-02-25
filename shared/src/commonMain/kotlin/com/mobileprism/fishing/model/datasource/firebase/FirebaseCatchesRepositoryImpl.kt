package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.CATCHES_COLLECTION
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class FirebaseCatchesRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker,
    private val connectionManager: ConnectionManager,
    private val authRepository: AuthRepository
) : CatchesRepositoryRead, CatchesRepositoryUpdate {

    override fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>> {
        val userId = authRepository.getCurrentUserId()
        return dbCollections.db.collectionGroup(CATCHES_COLLECTION)
            .where { "userId" equalTo userId }
            .orderBy("date", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                val result = ContentStateOld<UserCatch>()
                for (dc in snapshot.documentChanges) {
                    val userCatch = dc.document.data<UserCatch>()
                    when (dc.type) {
                        ChangeType.ADDED -> result.added.add(userCatch)
                        ChangeType.MODIFIED -> result.modified.add(userCatch)
                        ChangeType.REMOVED -> result.deleted.add(userCatch)
                    }
                }
                result
            }
    }

    override fun getAllUserCatchesList(): Flow<List<UserCatch>> {
        val userId = authRepository.getCurrentUserId()
        return dbCollections.db.collectionGroup(CATCHES_COLLECTION)
            .where { "userId" equalTo userId }
            .orderBy("date", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<UserCatch>() }
            }
    }

    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        return dbCollections.getUserCatchesCollection(markerId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<UserCatch>() }
            }
    }

    override fun addNewCatch(
        markerId: String,
        newCatch: UserCatch
    ): Flow<Result<Nothing?>> = channelFlow {
        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available

        try {
            dbCollections.getUserCatchesCollection(markerId).document(newCatch.id)
                .set(newCatch)
            if (isOnline) {
                analyticsTracker.logEvent(AnalyticsEvent.NewCatch)
            } else {
                analyticsTracker.logEvent(AnalyticsEvent.NewCatchOffline)
            }
            send(Result.success(null))
            incrementNumOfCatches(markerId)
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        try {
            dbCollections.getUserCatchesCollection(userCatch.userMarkerId).document(userCatch.id)
                .delete()
            decrementNumOfCatches(userCatch.userMarkerId)
        } catch (e: Exception) {
            println("Fishing: deleteCatch failed: ${e.message}")
        }
    }

    override suspend fun updateUserCatch(
        markerId: String,
        catchId: String,
        data: Map<String, Any>
    ) {
        dbCollections.getUserCatchesCollection(markerId).document(catchId)
            .update(data)
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<String>
    ): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading(0))

        try {
            dbCollections.getUserCatchesCollection(markerId).document(catchId)
                .update("downloadPhotoLinks" to newPhotos)
            flow.tryEmit(Progress.Complete)
        } catch (e: Exception) {
            println("Fishing: updateUserCatchPhotos failed: ${e.message}")
            flow.tryEmit(Progress.Complete)
        }

        return flow
    }

    override fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch> {
        return dbCollections.getUserCatchesCollection(markerId)
            .where { "id" equalTo catchId }
            .snapshots
            .mapNotNull { snapshot ->
                snapshot.documentChanges
                    .filter { it.type == ChangeType.MODIFIED }
                    .map { it.document.data<UserCatch>() }
                    .firstOrNull()
            }
    }

    private suspend fun incrementNumOfCatches(markerId: String) {
        try {
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("catchesCount" to FieldValue.increment(1))
        } catch (e: Exception) {
            println("Fishing: incrementNumOfCatches failed: ${e.message}")
        }
    }

    private suspend fun decrementNumOfCatches(markerId: String) {
        try {
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("catchesCount" to FieldValue.increment(-1))
        } catch (e: Exception) {
            println("Fishing: decrementNumOfCatches failed: ${e.message}")
        }
    }
}
