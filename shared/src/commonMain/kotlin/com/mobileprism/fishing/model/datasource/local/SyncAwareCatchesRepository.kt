package com.mobileprism.fishing.model.datasource.local

import androidx.paging.PagingData
import org.kimplify.cedar.logging.Cedar
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.SortDirection
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.local.entity.PendingOperationEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import com.mobileprism.fishing.model.datasource.local.mapper.toEntity
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import com.mobileprism.fishing.model.datasource.local.sync.payloadToJsonObject
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

class SyncAwareCatchesRepository(
    private val firebaseRepo: CatchesRepository,
    private val catchDao: CatchDao,
    private val pendingOpsDao: PendingOperationDao,
    private val connectionManager: ConnectionManager,
    private val syncScheduler: SyncScheduler,
    private val db: FishingDatabase,
    private val authRepository: AuthRepository,
) : CatchesRepository, AutoCloseable {

    companion object {
        private const val TAG = "SyncAwareCatches"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            connectionManager.getConnectionStateFlow().collect { state ->
                if (state is ConnectionState.Available) {
                    syncScheduler.scheduleSync()
                }
            }
        }
    }

    override fun getAllUserCatchesList(): Flow<List<UserCatch>> {
        return firebaseRepo.getAllUserCatchesList().map { catches ->
            catches.forEach { catch ->
                try {
                    catchDao.insert(catch.toEntity(SyncStatus.SYNCED))
                } catch (e: Exception) {
                    Cedar.tag(TAG).w("Failed to cache catch ${catch.id} locally: ${e.message}")
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
            Cedar.tag(TAG).e("Failed to update catch online, queuing: ${e.message}")
        }
        db.withTransaction {
            catchDao.updateSyncStatus(catchId, SyncStatus.PENDING_UPDATE)
            pendingOpsDao.deleteByEntity("catch", catchId)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = catchId,
                    operationType = "update",
                    parentId = markerId,
                    payload = Json.encodeToString(payloadToJsonObject(data)),
                    userId = authRepository.getCurrentUserId()
                )
            )
        }
        syncScheduler.scheduleSync()
        return Result.success(Unit)
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<String>
    ): Result<Unit> {
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
            Cedar.tag(TAG).e("Failed to delete catch online, queuing: ${e.message}")
        }
        db.withTransaction {
            catchDao.updateSyncStatus(userCatch.id, SyncStatus.PENDING_DELETE)
            pendingOpsDao.deleteByEntity("catch", userCatch.id)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "catch",
                    entityId = userCatch.id,
                    operationType = "delete",
                    parentId = userCatch.userMarkerId,
                    payload = Json.encodeToString(userCatch),
                    userId = authRepository.getCurrentUserId()
                )
            )
        }
        syncScheduler.scheduleSync()
        return Result.success(Unit)
    }

    override suspend fun addNewCatch(markerId: String, newCatch: UserCatch): Result<Unit> {
        try {
            catchDao.insert(newCatch.toEntity(SyncStatus.PENDING_CREATE))
        } catch (e: Exception) {
            Cedar.tag(TAG).w("Failed to cache new catch locally: ${e.message}")
        }

        try {
            val result = firebaseRepo.addNewCatch(markerId, newCatch)
            result.onSuccess {
                catchDao.updateSyncStatus(newCatch.id, SyncStatus.SYNCED)
                pendingOpsDao.deleteByEntity("catch", newCatch.id)
            }.onFailure {
                Cedar.tag(TAG).e("Failed to add catch online, queuing: ${it.message}")
                queueCatchCreate(markerId, newCatch)
            }
        } catch (e: Exception) {
            Cedar.tag(TAG).e("Exception adding catch online, queuing: ${e.message}")
            queueCatchCreate(markerId, newCatch)
        }
        return Result.success(Unit)
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
                    payload = Json.encodeToString(newCatch),
                    userId = authRepository.getCurrentUserId()
                )
            )
        }
        syncScheduler.scheduleSync()
    }

    override fun getAllUserCatchesPaged(
        sortField: String,
        sortDirection: SortDirection
    ): Flow<PagingData<UserCatch>> {
        return firebaseRepo.getAllUserCatchesPaged(sortField, sortDirection)
    }

    override fun close() { scope.cancel() }
}
