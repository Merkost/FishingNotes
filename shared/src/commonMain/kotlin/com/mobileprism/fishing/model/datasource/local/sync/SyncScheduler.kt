package com.mobileprism.fishing.model.datasource.local.sync

interface SyncScheduler {
    fun scheduleSync()
    fun schedulePeriodicSync()
}
