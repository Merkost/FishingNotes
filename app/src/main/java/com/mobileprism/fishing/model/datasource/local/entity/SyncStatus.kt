package com.mobileprism.fishing.model.datasource.local.entity

object SyncStatus {
    const val SYNCED = 0
    const val PENDING_CREATE = 1
    const val PENDING_UPDATE = 2
    const val PENDING_DELETE = 3
    const val CONFLICT = 4
}
