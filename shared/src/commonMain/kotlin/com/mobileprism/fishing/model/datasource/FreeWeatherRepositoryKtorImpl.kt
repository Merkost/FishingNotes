package com.mobileprism.fishing.model.datasource

import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.model.utils.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FreeWeatherRepositoryKtorImpl(
    private val analyticsTracker: AnalyticsTracker,
    private val rapidApiKey: String,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FreeWeatherRepository {

    companion object {
        private const val BASE_URL = "https://weather-by-api-ninjas.p.rapidapi.com"
    }

    override suspend fun getCurrentWeatherFree(
        lat: Double,
        lon: Double
    ): Flow<Result<CurrentWeatherFree>> = flow {
        emit(safeApiCall(dispatcher) {
            analyticsTracker.logEvent(AnalyticsEvent.GetFreeWeather)
            httpClient.get("$BASE_URL/v1/weather") {
                parameter("lat", lat)
                parameter("lon", lon)
                header("x-rapidapi-host", "weather-by-api-ninjas.p.rapidapi.com")
                header("x-rapidapi-key", rapidApiKey)
            }.body()
        })
    }
}
