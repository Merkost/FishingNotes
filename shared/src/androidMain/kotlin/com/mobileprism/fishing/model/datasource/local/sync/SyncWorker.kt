package com.mobileprism.fishing.model.datasource.local.sync

import android.content.Context
import androidx.work.CoroutineWorker
import org.kimplify.cedar.logging.Cedar
import androidx.work.WorkerParameters
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.MarkerDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.local.dao.WeatherCacheDao
import com.mobileprism.fishing.model.datasource.local.entity.PendingOperationEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val catchesRepo: CatchesRepository by inject(named("firebase"))
    private val markersRepo: MarkersRepositoryPaged by inject(named("firebase"))
    private val pendingOpsDao: PendingOperationDao by inject()
    private val catchDao: CatchDao by inject()
    private val markerDao: MarkerDao by inject()
    private val weatherCacheDao: WeatherCacheDao by inject()
    private val authRepository: AuthRepository by inject()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_RETRIES = 5
    }

    override suspend fun doWork(): Result {
        Cedar.tag(TAG).d("Starting sync work")

        val cutoff24h = (Clock.System.now() - 24.hours).toEpochMilliseconds()
        weatherCacheDao.deleteExpired(cutoff24h)

        val currentUserId = authRepository.getCurrentUserIdOrNull()
        if (currentUserId == null) {
            Cedar.tag(TAG).d("No signed-in user, skipping sync")
            return Result.success()
        }

        pendingOpsDao.deleteAllNotForUser(currentUserId)
        val pendingOps = pendingOpsDao.getAllForUser(currentUserId)
        if (pendingOps.isEmpty()) {
            Cedar.tag(TAG).d("No pending operations")
            return Result.success()
        }

        var hasFailures = false

        for (op in pendingOps) {
            if (authRepository.getCurrentUserIdOrNull() != currentUserId) {
                Cedar.tag(TAG).d("Signed-in user changed during sync, aborting")
                return Result.success()
            }
            try {
                val success = withTimeoutOrNull(30_000L) {
                    processOperation(op)
                } ?: false
                if (success) {
                    pendingOpsDao.delete(op)
                    Cedar.tag(TAG).d("Successfully synced: ${op.entityType}/${op.entityId}/${op.operationType}")
                } else {
                    handleFailure(op, "Operation returned false")
                    hasFailures = true
                }
            } catch (e: Exception) {
                Cedar.tag(TAG).e("Failed to sync: ${op.entityType}/${op.entityId}")
                handleFailure(op, e.message ?: "Unknown error")
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private suspend fun processOperation(op: PendingOperationEntity): Boolean {
        // Dedup check: verify entity still has expected pending status
        if (!isOperationStillValid(op)) {
            Cedar.tag(TAG).d("Skipping stale op: ${op.entityType}/${op.entityId}/${op.operationType}")
            return true // Treat as success — no longer needs processing
        }

        return when (op.entityType) {
            "catch" -> processCatchOperation(op)
            "marker" -> processMarkerOperation(op)
            else -> {
                Cedar.tag(TAG).w("Unknown entity type: ${op.entityType}")
                false
            }
        }
    }

    private suspend fun isOperationStillValid(op: PendingOperationEntity): Boolean {
        val expectedStatus = when (op.operationType) {
            "create" -> SyncStatus.PENDING_CREATE
            "update" -> SyncStatus.PENDING_UPDATE
            "delete" -> SyncStatus.PENDING_DELETE
            else -> return true // Unknown op type — let it proceed
        }
        val actualStatus = when (op.entityType) {
            "catch" -> catchDao.getById(op.entityId).firstOrNull()?.syncStatus
            "marker" -> markerDao.getById(op.entityId)?.syncStatus
            else -> return true
        }
        // Entity deleted or already synced — skip
        return actualStatus == expectedStatus
    }

    private suspend fun processCatchOperation(op: PendingOperationEntity): Boolean {
        return when (op.operationType) {
            "create" -> {
                val catch = json.decodeFromString<UserCatch>(op.payload)
                val result = catchesRepo.addNewCatch(op.parentId, catch)
                if (result.isSuccess) {
                    catchDao.updateSyncStatus(op.entityId, SyncStatus.SYNCED)
                }
                result.isSuccess
            }
            "update" -> {
                val jsonObject = json.parseToJsonElement(op.payload).jsonObject
                val data = jsonObjectToPayload(jsonObject).filterNot { (key, value) ->
                    (key == "note" || key == "downloadPhotoLinks") && value is String
                }
                if (data.isEmpty()) {
                    catchDao.updateSyncStatus(op.entityId, SyncStatus.SYNCED)
                    return true
                }
                val result = catchesRepo.updateUserCatch(op.parentId, op.entityId, data)
                if (result.isSuccess) {
                    catchDao.updateSyncStatus(op.entityId, SyncStatus.SYNCED)
                }
                result.isSuccess
            }
            "delete" -> {
                val catch = json.decodeFromString<UserCatch>(op.payload)
                val result = catchesRepo.deleteCatch(catch)
                if (result.isSuccess) {
                    catchDao.deleteByCatchId(op.entityId)
                }
                result.isSuccess
            }
            else -> false
        }
    }

    private suspend fun processMarkerOperation(op: PendingOperationEntity): Boolean {
        return when (op.operationType) {
            "create" -> {
                val marker = json.decodeFromString<UserMapMarker>(op.payload)
                val result = markersRepo.addNewMarker(marker)
                if (result.isSuccess) {
                    markerDao.updateSyncStatus(op.entityId, SyncStatus.SYNCED)
                }
                result.isSuccess
            }
            "delete" -> {
                val marker = json.decodeFromString<UserMapMarker>(op.payload)
                val result = markersRepo.deleteMarker(marker)
                if (result.isSuccess) {
                    markerDao.deleteByMarkerId(op.entityId)
                }
                result.isSuccess
            }
            else -> false
        }
    }

    private suspend fun handleFailure(op: PendingOperationEntity, error: String) {
        val newRetryCount = op.retryCount + 1
        if (newRetryCount >= MAX_RETRIES) {
            Cedar.tag(TAG).e("Max retries reached for ${op.entityType}/${op.entityId}, marking as conflict")
            when (op.entityType) {
                "catch" -> catchDao.updateSyncStatus(op.entityId, SyncStatus.CONFLICT)
                "marker" -> markerDao.updateSyncStatus(op.entityId, SyncStatus.CONFLICT)
            }
            pendingOpsDao.delete(op)
        } else {
            pendingOpsDao.update(op.copy(retryCount = newRetryCount, lastError = error))
        }
    }
}
