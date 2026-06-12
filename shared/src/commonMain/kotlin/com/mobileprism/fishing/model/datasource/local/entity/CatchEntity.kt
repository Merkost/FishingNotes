package com.mobileprism.fishing.model.datasource.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(
    tableName = "catches",
    foreignKeys = [
        ForeignKey(
            entity = MarkerEntity::class,
            parentColumns = ["id"],
            childColumns = ["userMarkerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userMarkerId"), Index("userId"), Index("userId", "date")]
)
data class CatchEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val description: String = "",
    val noteId: String = "",
    val noteTitle: String = "",
    val noteDescription: String = "",
    val noteDateCreated: Long = 0,
    val date: Long = 0,
    val fishType: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val fishingRodType: String = "",
    val fishingBait: String = "",
    val fishingLure: String = "",
    val userMarkerId: String = "",
    val placeTitle: String = "",
    val isPublic: Boolean = false,
    val downloadPhotoLinks: String = "[]",
    val weatherPrimary: String = "",
    val weatherIcon: String = "01",
    val weatherTemperature: Float = 0.0f,
    val weatherWindSpeed: Float = 0.0f,
    val weatherWindDeg: Int = 0,
    val weatherPressure: Int = 0,
    val weatherMoonPhase: Float = 0.0f,
    val syncStatus: Int = SyncStatus.SYNCED,
    val lastModified: Long = Clock.System.now().toEpochMilliseconds()
)
