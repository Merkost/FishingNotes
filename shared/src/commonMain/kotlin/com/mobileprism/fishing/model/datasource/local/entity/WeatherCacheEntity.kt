package com.mobileprism.fishing.model.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cacheKey: String,
    val latitude: Double,
    val longitude: Double,
    val responseJson: String,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val ttlMillis: Long = 3_600_000L
) {
    fun isExpired(): Boolean {
        if (ttlMillis == Long.MAX_VALUE) return false
        return Clock.System.now().toEpochMilliseconds() - cachedAt > ttlMillis
    }
}
