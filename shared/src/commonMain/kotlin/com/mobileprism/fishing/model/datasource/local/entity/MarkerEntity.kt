package com.mobileprism.fishing.model.datasource.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(
    tableName = "markers",
    indices = [Index("userId"), Index("userId", "dateOfCreation")]
)
data class MarkerEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "My marker",
    val description: String = "",
    val markerColor: Int = 0,
    val catchesCount: Int = 0,
    val dateOfCreation: Long = 0,
    val visible: Boolean = true,
    val public: Boolean = false,
    val notes: String = "[]",
    val syncStatus: Int = SyncStatus.SYNCED,
    val lastModified: Long = Clock.System.now().toEpochMilliseconds()
)
