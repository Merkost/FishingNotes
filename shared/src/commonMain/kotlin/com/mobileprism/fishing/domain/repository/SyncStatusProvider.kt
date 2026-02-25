package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.SyncState
import kotlinx.coroutines.flow.StateFlow

interface SyncStatusProvider {
    val globalSyncState: StateFlow<SyncState>
}
