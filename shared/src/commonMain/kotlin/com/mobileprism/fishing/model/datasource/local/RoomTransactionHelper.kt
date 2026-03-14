package com.mobileprism.fishing.model.datasource.local

import androidx.room.RoomDatabase
import androidx.room.util.performInTransactionSuspending

suspend fun <T> RoomDatabase.withTransaction(block: suspend () -> T): T {
    return performInTransactionSuspending(this) { block() }
}
