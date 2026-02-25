package com.mobileprism.fishing.model.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobileprism.fishing.model.datasource.local.entity.WeatherCacheEntity

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key")
    suspend fun getByKey(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cachedAt < :cutoff AND ttlMillis != ${Long.MAX_VALUE}")
    suspend fun deleteExpired(cutoff: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAll()
}
