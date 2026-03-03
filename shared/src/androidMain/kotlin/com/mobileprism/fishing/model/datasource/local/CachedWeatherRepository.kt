package com.mobileprism.fishing.model.datasource.local

import android.util.Log
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.entity.weather.WeatherResult
import com.mobileprism.fishing.domain.entity.weather.WeatherSource
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.datasource.local.dao.WeatherCacheDao
import com.mobileprism.fishing.model.datasource.local.entity.WeatherCacheEntity
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CachedWeatherRepository(
    private val remoteRepo: WeatherRepository,
    private val weatherCacheDao: WeatherCacheDao,
    private val connectionManager: ConnectionManager,
) : WeatherRepository {

    companion object {
        private const val TAG = "CachedWeatherRepo"
        private const val TTL_ONE_HOUR = 3_600_000L
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherForecast> {
        val cacheKey = "%.2f_%.2f".format(lat, lon)
        val cached = weatherCacheDao.getByKey(cacheKey)

        if (cached != null && !cached.isExpired()) {
            val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
            return Result.success(forecast)
        }

        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        if (isOnline) {
            val result = remoteRepo.getWeather(lat, lon)
            result.onSuccess { forecast ->
                val jsonStr = json.encodeToString(forecast)
                weatherCacheDao.insert(
                    WeatherCacheEntity(
                        cacheKey = cacheKey,
                        latitude = lat,
                        longitude = lon,
                        responseJson = jsonStr,
                        ttlMillis = TTL_ONE_HOUR
                    )
                )
                return Result.success(forecast)
            }.onFailure { error ->
                if (cached != null) {
                    Log.d(TAG, "Remote fetch failed, returning stale cache", error)
                    val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
                    return Result.success(forecast)
                } else {
                    return Result.failure(error)
                }
            }
        }

        return if (cached != null) {
            val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
            Result.success(forecast)
        } else {
            Result.failure(Exception("No internet connection and no cached data"))
        }
    }

    override suspend fun getWeatherWithMeta(lat: Double, lon: Double): Result<WeatherResult> {
        val cacheKey = "%.2f_%.2f".format(lat, lon)
        val cached = weatherCacheDao.getByKey(cacheKey)

        if (cached != null && !cached.isExpired()) {
            val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
            return Result.success(
                WeatherResult(forecast, WeatherSource.CACHED, cached.cachedAt)
            )
        }

        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        if (isOnline) {
            val result = remoteRepo.getWeather(lat, lon)
            result.onSuccess { forecast ->
                val jsonStr = json.encodeToString(forecast)
                weatherCacheDao.insert(
                    WeatherCacheEntity(
                        cacheKey = cacheKey,
                        latitude = lat,
                        longitude = lon,
                        responseJson = jsonStr,
                        ttlMillis = TTL_ONE_HOUR
                    )
                )
                return Result.success(
                    WeatherResult(forecast, WeatherSource.FRESH)
                )
            }.onFailure { error ->
                if (cached != null) {
                    Log.d(TAG, "Remote fetch failed, returning stale cache", error)
                    val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
                    return Result.success(
                        WeatherResult(forecast, WeatherSource.STALE_FALLBACK, cached.cachedAt)
                    )
                } else {
                    return Result.failure(error)
                }
            }
        }

        return if (cached != null) {
            val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
            Result.success(
                WeatherResult(forecast, WeatherSource.STALE_FALLBACK, cached.cachedAt)
            )
        } else {
            Result.failure(Exception("No internet connection and no cached data"))
        }
    }

    override suspend fun getHistoricalWeather(
        lat: Double,
        lon: Double,
        date: Long
    ): Result<WeatherForecast> {
        val cacheKey = "%.2f_%.2f_%d".format(lat, lon, date)
        val cached = weatherCacheDao.getByKey(cacheKey)

        if (cached != null) {
            val forecast = json.decodeFromString<WeatherForecast>(cached.responseJson)
            return Result.success(forecast)
        }

        val isOnline = connectionManager.getConnectionState() is ConnectionState.Available
        if (isOnline) {
            val result = remoteRepo.getHistoricalWeather(lat, lon, date)
            result.onSuccess { forecast ->
                val jsonStr = json.encodeToString(forecast)
                weatherCacheDao.insert(
                    WeatherCacheEntity(
                        cacheKey = cacheKey,
                        latitude = lat,
                        longitude = lon,
                        responseJson = jsonStr,
                        ttlMillis = Long.MAX_VALUE
                    )
                )
                return Result.success(forecast)
            }.onFailure { error ->
                return Result.failure(error)
            }
        }

        return Result.failure(Exception("No internet connection and no cached historical data"))
    }
}
