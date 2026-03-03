package com.mobileprism.fishing.model.datasource.local

import android.util.Log
import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import androidx.room.withTransaction
import com.mobileprism.fishing.model.datasource.local.dao.MarkerDao
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Closeable

class SyncAwareMarkersRepository(
    private val firebaseRepo: MarkersRepositoryPaged,
    private val markerDao: MarkerDao,
    private val pendingOpsDao: PendingOperationDao,
    private val connectionManager: ConnectionManager,
    private val syncScheduler: SyncScheduler,
    private val db: FishingDatabase,
) : MarkersRepositoryPaged, Closeable {

    companion object {
        private const val TAG = "SyncAwareMarkers"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            connectionManager.getConnectionStateFlow().collect { state ->
                if (state is ConnectionState.Available) {
                    syncScheduler.scheduleSync()
                }
            }
        }
    }

    override suspend fun getMapMarker(markerId: String): Result<UserMapMarker> {
        return firebaseRepo.getMapMarker(markerId)
    }

    override fun getAllUserMarkers(): Flow<ContentState<MapMarker>> {
        return firebaseRepo.getAllUserMarkers()
    }

    override fun getAllUserMarkersList(): Flow<List<UserMapMarker>> {
        return firebaseRepo.getAllUserMarkersList()
    }

    override suspend fun saveNewNote(markerId: String, newNote: Note): Result<Unit> {
        return firebaseRepo.saveNewNote(markerId, newNote)
    }

    override suspend fun updateNotes(markerId: String, notes: List<Note>): Result<Unit> {
        return firebaseRepo.updateNotes(markerId, notes)
    }

    override suspend fun changeMarkerVisibility(
        marker: UserMapMarker,
        changeTo: Boolean
    ): Result<Unit> {
        return firebaseRepo.changeMarkerVisibility(marker, changeTo)
    }

    override fun getAllUserMarkersListPaged(
        sortField: String,
        sortDirection: Query.Direction
    ): Flow<PagingData<UserMapMarker>> {
        return firebaseRepo.getAllUserMarkersListPaged(sortField, sortDirection)
    }

    override suspend fun deleteMarker(userMapMarker: UserMapMarker): Result<Unit> {
        try {
            val result = firebaseRepo.deleteMarker(userMapMarker)
            if (result.isSuccess) {
                markerDao.deleteByMarkerId(userMapMarker.id)
                pendingOpsDao.deleteByEntity("marker", userMapMarker.id)
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete marker online, queuing", e)
        }
        // Queue for offline sync
        db.withTransaction {
            markerDao.updateSyncStatus(userMapMarker.id, SyncStatus.PENDING_DELETE)
            pendingOpsDao.deleteByEntity("marker", userMapMarker.id)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "marker",
                    entityId = userMapMarker.id,
                    operationType = "delete",
                    payload = Json.encodeToString(userMapMarker)
                )
            )
        }
        syncScheduler.scheduleSync()
        return Result.success(Unit) // Success from local perspective
    }

    override fun close() { scope.cancel() }

    override suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit> {
        // Save locally immediately
        markerDao.insert(newMarker.toEntity(SyncStatus.PENDING_CREATE))

        try {
            val result = firebaseRepo.addNewMarker(newMarker)
            result.onSuccess {
                markerDao.updateSyncStatus(newMarker.id, SyncStatus.SYNCED)
                pendingOpsDao.deleteByEntity("marker", newMarker.id)
                return result
            }.onFailure {
                Log.e(TAG, "Failed to add marker online, queuing", it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding marker online, queuing", e)
        }
        // Queue for offline sync
        db.withTransaction {
            pendingOpsDao.deleteByEntity("marker", newMarker.id)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "marker",
                    entityId = newMarker.id,
                    operationType = "create",
                    payload = Json.encodeToString(newMarker)
                )
            )
        }
        syncScheduler.scheduleSync()
        return Result.success(Unit) // Success from local perspective
    }
}
