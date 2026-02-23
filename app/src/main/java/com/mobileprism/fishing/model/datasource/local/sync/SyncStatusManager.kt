package com.mobileprism.fishing.model.datasource.local.sync

import com.mobileprism.fishing.domain.entity.common.SyncState
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.Closeable

class SyncStatusManager(
    private val pendingOpsDao: PendingOperationDao,
    private val connectionManager: ConnectionManager
) : Closeable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _globalSyncState = MutableStateFlow<SyncState>(SyncState.Synced)
    val globalSyncState: StateFlow<SyncState> = _globalSyncState.asStateFlow()

    val pendingCount: Flow<Int> = pendingOpsDao.observeCount()

    init {
        scope.launch {
            combine(
                pendingOpsDao.observeCount(),
                connectionManager.getConnectionStateFlow()
            ) { count, connection ->
                when {
                    count == 0 -> SyncState.Synced
                    connection is ConnectionState.Available -> SyncState.Pending
                    else -> SyncState.Error("No internet connection")
                }
            }.collect { state ->
                _globalSyncState.value = state
            }
        }
    }

    override fun close() { scope.cancel() }
}
