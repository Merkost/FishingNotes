package com.mobileprism.fishing.model.datasource.local.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.MarkerDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.local.dao.WeatherCacheDao
import com.mobileprism.fishing.model.datasource.local.entity.PendingOperationEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

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
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_RETRIES = 5
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work")

        val cutoff24h = (Clock.System.now() - 24.hours).toEpochMilliseconds()
        weatherCacheDao.deleteExpired(cutoff24h)

        val pendingOps = pendingOpsDao.getAll()
        if (pendingOps.isEmpty()) {
            Log.d(TAG, "No pending operations")
            return Result.success()
        }

        var hasFailures = false

        for (op in pendingOps) {
            try {
                val success = processOperation(op)
                if (success) {
                    pendingOpsDao.delete(op)
                    Log.d(TAG, "Successfully synced: ${op.entityType}/${op.entityId}/${op.operationType}")
                } else {
                    handleFailure(op, "Operation returned false")
                    hasFailures = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync: ${op.entityType}/${op.entityId}", e)
                handleFailure(op, e.message ?: "Unknown error")
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private suspend fun processOperation(op: PendingOperationEntity): Boolean {
        return when (op.entityType) {
            "catch" -> processCatchOperation(op)
            "marker" -> processMarkerOperation(op)
            else -> {
                Log.w(TAG, "Unknown entity type: ${op.entityType}")
                false
            }
        }
    }

    private suspend fun processCatchOperation(op: PendingOperationEntity): Boolean {
        return when (op.operationType) {
            "create" -> {
                val catch = json.decodeFromString<UserCatch>(op.payload)
                val result = catchesRepo.addNewCatch(op.parentId, catch).first()
                if (result.isSuccess) {
                    catchDao.updateSyncStatus(op.entityId, SyncStatus.SYNCED)
                }
                result.isSuccess
            }
            "update" -> {
                val jsonObject = json.parseToJsonElement(op.payload).jsonObject
                val data: Map<String, Any> = jsonObject.mapValues { (_, element) ->
                    val primitive = element.jsonPrimitive
                    when {
                        primitive.isString -> primitive.content
                        primitive.booleanOrNull != null -> primitive.content.toBoolean()
                        primitive.longOrNull != null -> primitive.content.toLong()
                        primitive.doubleOrNull != null -> primitive.content.toDouble()
                        else -> primitive.content
                    } as Any
                }
                catchesRepo.updateUserCatch(op.parentId, op.entityId, data)
                catchDao.updateSyncStatus(op.entityId, SyncStatus.SYNCED)
                true
            }
            "delete" -> {
                val catch = json.decodeFromString<UserCatch>(op.payload)
                catchesRepo.deleteCatch(catch)
                catchDao.deleteByCatchId(op.entityId)
                true
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
                markersRepo.deleteMarker(marker)
                markerDao.deleteByMarkerId(op.entityId)
                true
            }
            else -> false
        }
    }

    private suspend fun handleFailure(op: PendingOperationEntity, error: String) {
        val newRetryCount = op.retryCount + 1
        if (newRetryCount >= MAX_RETRIES) {
            Log.e(TAG, "Max retries reached for ${op.entityType}/${op.entityId}, marking as conflict")
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
