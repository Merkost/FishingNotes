package com.mobileprism.fishing.domain.entity.weather

data class WeatherResult(
    val forecast: WeatherForecast,
    val source: WeatherSource,
    val cachedAtMillis: Long? = null
)

enum class WeatherSource {
    FRESH,
    CACHED,
    STALE_FALLBACK
}
