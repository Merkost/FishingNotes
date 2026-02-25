package com.mobileprism.fishing.model.datasource.local

import android.util.Log
import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.LiteProgress
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
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
import kotlinx.coroutines.flow.StateFlow
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

    override suspend fun saveNewNote(markerId: String, newNote: Note): Flow<Result<Unit>> {
        return firebaseRepo.saveNewNote(markerId, newNote)
    }

    override suspend fun updateNotes(markerId: String, notes: List<Note>): Flow<Result<Unit>> {
        return firebaseRepo.updateNotes(markerId, notes)
    }

    override suspend fun changeMarkerVisibility(
        marker: UserMapMarker,
        changeTo: Boolean
    ): StateFlow<LiteProgress> {
        return firebaseRepo.changeMarkerVisibility(marker, changeTo)
    }

    override fun getAllUserMarkersListPaged(
        sortField: String,
        sortDirection: Query.Direction
    ): Flow<PagingData<UserMapMarker>> {
        return firebaseRepo.getAllUserMarkersListPaged(sortField, sortDirection)
    }

    override suspend fun deleteMarker(userMapMarker: UserMapMarker) {
        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        if (isOnline) {
            try {
                firebaseRepo.deleteMarker(userMapMarker)
                markerDao.deleteByMarkerId(userMapMarker.id)
                pendingOpsDao.deleteByEntity("marker", userMapMarker.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete marker online, queuing", e)
                markerDao.updateSyncStatus(userMapMarker.id, SyncStatus.PENDING_DELETE)
                pendingOpsDao.insert(
                    PendingOperationEntity(
                        entityType = "marker",
                        entityId = userMapMarker.id,
                        operationType = "delete",
                        payload = Json.encodeToString(userMapMarker)
                    )
                )
                syncScheduler.scheduleSync()
            }
        } else {
            markerDao.updateSyncStatus(userMapMarker.id, SyncStatus.PENDING_DELETE)
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "marker",
                    entityId = userMapMarker.id,
                    operationType = "delete",
                    payload = Json.encodeToString(userMapMarker)
                )
            )
            syncScheduler.scheduleSync()
        }
    }

    override fun close() { scope.cancel() }

    override suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit> {
        // Save locally immediately
        markerDao.insert(newMarker.toEntity(SyncStatus.PENDING_CREATE))

        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        return if (isOnline) {
            try {
                val result = firebaseRepo.addNewMarker(newMarker)
                result.onSuccess {
                    markerDao.updateSyncStatus(newMarker.id, SyncStatus.SYNCED)
                    pendingOpsDao.deleteByEntity("marker", newMarker.id)
                }.onFailure {
                    Log.e(TAG, "Failed to add marker online, queuing", it)
                    pendingOpsDao.insert(
                        PendingOperationEntity(
                            entityType = "marker",
                            entityId = newMarker.id,
                            operationType = "create",
                            payload = Json.encodeToString(newMarker)
                        )
                    )
                    syncScheduler.scheduleSync()
                }
                result
            } catch (e: Exception) {
                Log.e(TAG, "Exception adding marker online, queuing", e)
                pendingOpsDao.insert(
                    PendingOperationEntity(
                        entityType = "marker",
                        entityId = newMarker.id,
                        operationType = "create",
                        payload = Json.encodeToString(newMarker)
                    )
                )
                syncScheduler.scheduleSync()
                Result.success(Unit) // Success from local perspective
            }
        } else {
            pendingOpsDao.insert(
                PendingOperationEntity(
                    entityType = "marker",
                    entityId = newMarker.id,
                    operationType = "create",
                    payload = Json.encodeToString(newMarker)
                )
            )
            syncScheduler.scheduleSync()
            Result.success(Unit)
        }
    }
}
