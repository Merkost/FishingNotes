package com.mobileprism.fishing.model.datasource.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datasource.room.dao.CatchesDao
import com.mobileprism.fishing.model.datasource.room.dao.MapMarkersDao

private const val DB_NAME = "fishing_database"

@Database(entities = [(UserMapMarker::class), (UserCatch::class)], version = 4)
@TypeConverters(Converters::class)
abstract class FishingDatabase : RoomDatabase() {

    abstract fun mapMarkersDao(): MapMarkersDao
    abstract fun catchesDao(): CatchesDao

    companion object {
        fun create(context: Context): FishingDatabase {

            return Room.databaseBuilder(
                context,
                FishingDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}