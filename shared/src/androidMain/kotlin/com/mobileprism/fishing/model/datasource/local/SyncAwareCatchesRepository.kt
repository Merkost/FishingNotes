package com.mobileprism.fishing.model.datasource.local

import android.util.Log
import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import androidx.room.withTransaction
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.local.entity.PendingOperationEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import com.mobileprism.fishing.model.datasource.local.mapper.toEntity
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.Closeable

class SyncAwareCatchesRepository(
    private val firebaseRepo: CatchesRepository,
    private val catchDao: CatchDao,
    private val pendingOpsDao: PendingOperationDao,
    private val connectionManager: ConnectionManager,
    private val syncScheduler: SyncScheduler,
    private val db: FishingDatabase,
) : CatchesRepository, Closeable {

    companion object {
        private const val TAG = "SyncAwareCatches"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Auto-sync when connectivity restored
        scope.launch {
            connectionManager.getConnectionStateFlow().collect { state ->
                if (state is ConnectionState.Available) {
                    syncScheduler.scheduleSync()
                }
            }
        }
    }

    override fun getAllUserCatchesList(): Flow<List<UserCatch>> {
        // Primary source is Firebase snapshot listeners; cache results to Room as side effect
        return firebaseRepo.getAllUserCatchesList().map { catches ->
            // Cache to Room as side effect — ignore FK failures when marker isn't cached yet
            catches.forEach { catch ->
                try {
                    catchDao.insert(catch.toEntity(SyncStatus.SYNCED))
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to cache catch ${catch.id} locally: ${e.message}")
                }
            }
            catches
        }
    }

    override fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>> {
        return firebaseRepo.getAllUserCatchesState()
    }

    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        return firebaseRepo.getCatchesByMarkerId(markerId)
    }

    override fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch> {
        return firebaseRepo.subscribeOnUserCatchState(markerId, catchId)
    }

    override suspend fun updateUserCatch(markerId: String, catchId: String, data: Map<String, Any>): Result<Unit> {
        try {
            val result = firebaseRepo.updateUserCatch(markerId, catchId, data)
            if (result.isSuccess) {
                catchDao.updateSyncStatus(catchId, SyncStatus.SYNCED)
                return result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update catch online, queuing", e)
        }
        // Queue for offline sync
        db.withTransaction {
            catchDao.updateSyncStatus(catchId, SyncStatus.PENDING_UPDATE)
            pendingOpsDao.deleteByEntity("catch", catchId)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = catchId,
                    operationType = "update",
                    parentId = markerId,
                    payload = Json.encodeToString(mapToJsonObject(data))
                )
            )
        }
        syncScheduler.scheduleSync()
        return Result.success(Unit) // Success from local perspective
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<String>
    ): Result<Unit> {
        // Photo uploads require network, delegate directly
        return firebaseRepo.updateUserCatchPhotos(markerId, catchId, newPhotos)
    }

    override suspend fun deleteCatch(userCatch: UserCatch): Result<Unit> {
        try {
            val result = firebaseRepo.deleteCatch(userCatch)
            if (result.isSuccess) {
                catchDao.deleteByCatchId(userCatch.id)
                pendingOpsDao.deleteByEntity("catch", userCatch.id)
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete catch online, queuing", e)
        }
        // Queue for offline sync
        db.withTransaction {
            catchDao.updateSyncStatus(userCatch.id, SyncStatus.PENDING_DELETE)
            pendingOpsDao.deleteByEntity("catch", userCatch.id)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = userCatch.id,
                    operationType = "delete",
                    parentId = userCatch.userMarkerId,
                    payload = Json.encodeToString(userCatch)
                )
            )
        }
        syncScheduler.scheduleSync()
        return Result.success(Unit) // Success from local perspective
    }

    override suspend fun addNewCatch(markerId: String, newCatch: UserCatch): Result<Unit> {
        // Save locally immediately
        try {
            catchDao.insert(newCatch.toEntity(SyncStatus.PENDING_CREATE))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache new catch locally: ${e.message}")
        }

        try {
            val result = firebaseRepo.addNewCatch(markerId, newCatch)
            result.onSuccess {
                catchDao.updateSyncStatus(newCatch.id, SyncStatus.SYNCED)
                pendingOpsDao.deleteByEntity("catch", newCatch.id)
            }.onFailure {
                Log.e(TAG, "Failed to add catch online, queuing", it)
                queueCatchCreate(markerId, newCatch)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding catch online, queuing", e)
            queueCatchCreate(markerId, newCatch)
        }
        return Result.success(Unit) // Success from local perspective
    }

    private suspend fun queueCatchCreate(markerId: String, newCatch: UserCatch) {
        db.withTransaction {
            pendingOpsDao.deleteByEntity("catch", newCatch.id)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = newCatch.id,
                    operationType = "create",
                    parentId = markerId,
                    payload = Json.encodeToString(newCatch)
                )
            )
        }
        syncScheduler.scheduleSync()
    }

    override fun getAllUserCatchesPaged(
        sortField: String,
        sortDirection: Query.Direction
    ): Flow<PagingData<UserCatch>> {
        return firebaseRepo.getAllUserCatchesPaged(sortField, sortDirection)
    }

    override fun close() { scope.cancel() }

    private fun mapToJsonObject(map: Map<String, Any>): JsonObject {
        return JsonObject(map.mapValues { (_, v) ->
            when (v) {
                is String -> JsonPrimitive(v)
                is Number -> JsonPrimitive(v)
                is Boolean -> JsonPrimitive(v)
                else -> JsonPrimitive(v.toString())
            }
        })
    }
}
