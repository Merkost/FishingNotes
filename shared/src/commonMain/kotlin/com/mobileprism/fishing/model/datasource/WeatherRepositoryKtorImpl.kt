package com.mobileprism.fishing.model.datasource

import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.utils.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class WeatherRepositoryKtorImpl(
    private val analyticsTracker: AnalyticsTracker,
    private val openWeatherKey: String,
    private val languageTag: String,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val baseUrl: String = DEFAULT_BASE_URL,
) : WeatherRepository {

    init {
        require(openWeatherKey.isNotBlank()) { "OpenWeather API key must not be blank" }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.openweathermap.org/data/3.0"
    }

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherForecast> =
        safeApiCall(dispatcher) {
            analyticsTracker.logEvent(AnalyticsEvent.GetWeather)
            httpClient.get("$baseUrl/onecall") {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("units", "metric")
                parameter("exclude", "minutely,current,alerts")
                parameter("lang", languageTag)
                parameter("appid", openWeatherKey)
            }.body()
        }

    override suspend fun getHistoricalWeather(
        lat: Double,
        lon: Double,
        date: Long
    ): Result<WeatherForecast> = safeApiCall(dispatcher) {
        httpClient.get("$baseUrl/onecall/timemachine") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("dt", date)
            parameter("units", "metric")
            parameter("lang", languageTag)
            parameter("appid", openWeatherKey)
        }.body()
    }
}
