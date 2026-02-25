package com.mobileprism.fishing.domain.entity.common

sealed class SyncState {
    data object Synced : SyncState()
    data object Pending : SyncState()
    data class Error(val message: String? = null) : SyncState()
    data object Conflict : SyncState()
}
