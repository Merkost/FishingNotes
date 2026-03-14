package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.entity.common.ContentStateOld
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
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn

class FirebaseCatchesRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker,
    private val connectionManager: ConnectionManager,
    private val authRepository: AuthRepository
) : CatchesRepositoryRead, CatchesRepositoryUpdate, AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob())

    private val catchesSnapshotFlow: Flow<QuerySnapshot> by lazy {
        val userId = authRepository.getCurrentUserId()
        dbCollections.db.collectionGroup(CATCHES_COLLECTION)
            .where { "userId" equalTo userId }
            .orderBy("date", Direction.DESCENDING)
            .snapshots
            .shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)
    }

    override fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>> {
        return catchesSnapshotFlow
            .map { snapshot ->
                val result = ContentStateOld<UserCatch>()
                for (dc in snapshot.documentChanges) {
                    try {
                        val userCatch = dc.document.data<UserCatch>()
                        when (dc.type) {
                            ChangeType.ADDED -> result.added.add(userCatch)
                            ChangeType.MODIFIED -> result.modified.add(userCatch)
                            ChangeType.REMOVED -> result.deleted.add(userCatch)
                        }
                    } catch (_: Exception) { }
                }
                result
            }
            .catch { emit(ContentStateOld()) }
    }

    override fun getAllUserCatchesList(): Flow<List<UserCatch>> {
        return catchesSnapshotFlow
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<UserCatch>() } catch (_: Exception) { null }
                }
            }
            .catch { emit(emptyList()) }
    }

    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        return dbCollections.getUserCatchesCollection(markerId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<UserCatch>() } catch (_: Exception) { null }
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun addNewCatch(
        markerId: String,
        newCatch: UserCatch
    ): Result<Unit> {
        return try {
            val isOnline = connectionManager.getConnectionState() is ConnectionState.Available

            dbCollections.db.batch().apply {
                set(dbCollections.getUserCatchesCollection(markerId).document(newCatch.id), newCatch)
                updateFields(
                    dbCollections.getUserMapMarkersCollection().document(markerId)
                ) { "catchesCount" to FieldValue.increment(1) }
            }.commit()

            if (isOnline) {
                analyticsTracker.logEvent(AnalyticsEvent.NewCatch)
            } else {
                analyticsTracker.logEvent(AnalyticsEvent.NewCatchOffline)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCatch(userCatch: UserCatch): Result<Unit> {
        return try {
            dbCollections.db.batch().apply {
                delete(dbCollections.getUserCatchesCollection(userCatch.userMarkerId).document(userCatch.id))
                updateFields(
                    dbCollections.getUserMapMarkersCollection().document(userCatch.userMarkerId)
                ) { "catchesCount" to FieldValue.increment(-1) }
            }.commit()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserCatch(
        markerId: String,
        catchId: String,
        data: Map<String, Any>
    ): Result<Unit> {
        return try {
            dbCollections.getUserCatchesCollection(markerId).document(catchId)
                .update(data)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<String>
    ): Result<Unit> {
        return try {
            dbCollections.getUserCatchesCollection(markerId).document(catchId)
                .updateFields { "downloadPhotoLinks" to newPhotos }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    override fun close() {
        scope.cancel()
    }
}
