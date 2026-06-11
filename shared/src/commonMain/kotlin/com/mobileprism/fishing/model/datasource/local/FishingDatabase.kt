package com.mobileprism.fishing.model.datasource.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.mobileprism.fishing.model.datasource.local.converter.Converters
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.MarkerDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.local.dao.WeatherCacheDao
import com.mobileprism.fishing.model.datasource.local.entity.CatchEntity
import com.mobileprism.fishing.model.datasource.local.entity.MarkerEntity
import com.mobileprism.fishing.model.datasource.local.entity.PendingOperationEntity
import com.mobileprism.fishing.model.datasource.local.entity.WeatherCacheEntity

@Database(
    entities = [
        CatchEntity::class,
        MarkerEntity::class,
        PendingOperationEntity::class,
        WeatherCacheEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(FishingDatabaseConstructor::class)
abstract class FishingDatabase : RoomDatabase() {
    abstract fun catchDao(): CatchDao
    abstract fun markerDao(): MarkerDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun weatherCacheDao(): WeatherCacheDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object FishingDatabaseConstructor : RoomDatabaseConstructor<FishingDatabase> {
    override fun initialize(): FishingDatabase
}
