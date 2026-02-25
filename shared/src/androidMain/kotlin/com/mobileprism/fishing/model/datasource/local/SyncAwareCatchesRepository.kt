package com.mobileprism.fishing.model.datasource.local

import android.util.Log
import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
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

    override suspend fun updateUserCatch(markerId: String, catchId: String, data: Map<String, Any>) {
        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        if (isOnline) {
            try {
                firebaseRepo.updateUserCatch(markerId, catchId, data)
                catchDao.updateSyncStatus(catchId, SyncStatus.SYNCED)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update catch online, queuing", e)
                catchDao.updateSyncStatus(catchId, SyncStatus.PENDING_UPDATE)
                pendingOpsDao.insert(
                    PendingOperationEntity(
                        entityType = "catch",
                        entityId = catchId,
                        operationType = "update",
                        parentId = markerId,
                        payload = Json.encodeToString(mapToJsonObject(data))
                    )
                )
                syncScheduler.scheduleSync()
            }
        } else {
            catchDao.updateSyncStatus(catchId, SyncStatus.PENDING_UPDATE)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = catchId,
                    operationType = "update",
                    parentId = markerId,
                    payload = Json.encodeToString(mapToJsonObject(data))
                )
            )
            syncScheduler.scheduleSync()
        }
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<String>
    ): StateFlow<Progress> {
        // Photo uploads require network, delegate directly
        return firebaseRepo.updateUserCatchPhotos(markerId, catchId, newPhotos)
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        if (isOnline) {
            try {
                firebaseRepo.deleteCatch(userCatch)
                catchDao.deleteByCatchId(userCatch.id)
                pendingOpsDao.deleteByEntity("catch", userCatch.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete catch online, queuing", e)
                catchDao.updateSyncStatus(userCatch.id, SyncStatus.PENDING_DELETE)
                pendingOpsDao.insert(
                    PendingOperationEntity(
                        entityType = "catch",
                        entityId = userCatch.id,
                        operationType = "delete",
                        parentId = userCatch.userMarkerId,
                        payload = Json.encodeToString(userCatch)
                    )
                )
                syncScheduler.scheduleSync()
            }
        } else {
            catchDao.updateSyncStatus(userCatch.id, SyncStatus.PENDING_DELETE)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = userCatch.id,
                    operationType = "delete",
                    parentId = userCatch.userMarkerId,
                    payload = Json.encodeToString(userCatch)
                )
            )
            syncScheduler.scheduleSync()
        }
    }

    override fun addNewCatch(markerId: String, newCatch: UserCatch): Flow<Result<Nothing?>> {
        return channelFlow {
            // Save locally immediately
            try {
                catchDao.insert(newCatch.toEntity(SyncStatus.PENDING_CREATE))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cache new catch locally: ${e.message}")
            }

            val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
            if (isOnline) {
                firebaseRepo.addNewCatch(markerId, newCatch).collect { result ->
                    result.onSuccess {
                        catchDao.updateSyncStatus(newCatch.id, SyncStatus.SYNCED)
                        pendingOpsDao.deleteByEntity("catch", newCatch.id)
                    }.onFailure {
                        Log.e(TAG, "Failed to add catch online, queuing", it)
                        pendingOpsDao.insert(
                            PendingOperationEntity(
                                entityType = "catch",
                                entityId = newCatch.id,
                                operationType = "create",
                                parentId = markerId,
                                payload = Json.encodeToString(newCatch)
                            )
                        )
                        syncScheduler.scheduleSync()
                    }
                    send(result)
                }
            } else {
                pendingOpsDao.insert(
                    PendingOperationEntity(
                        entityType = "catch",
                        entityId = newCatch.id,
                        operationType = "create",
                        parentId = markerId,
                        payload = Json.encodeToString(newCatch)
                    )
                )
                syncScheduler.scheduleSync()
                send(Result.success(null))
            }
        }
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
