package com.mobileprism.fishing.model.datasource.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(
    tableName = "pending_operations",
    indices = [Index("entityType", "entityId"), Index("userId")]
)
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val operationType: String,
    val parentId: String = "",
    val payload: String = "",
    @ColumnInfo(defaultValue = "") val userId: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val retryCount: Int = 0,
    val lastError: String? = null
)
