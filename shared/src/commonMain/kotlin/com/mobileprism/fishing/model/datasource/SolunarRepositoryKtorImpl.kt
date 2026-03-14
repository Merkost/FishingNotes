package com.mobileprism.fishing.model.datasource

import com.mobileprism.fishing.domain.entity.solunar.Solunar
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.model.utils.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SolunarRepositoryKtorImpl(
    private val analyticsTracker: AnalyticsTracker,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val baseUrl: String = DEFAULT_BASE_URL,
) : SolunarRepository {

    companion object {
        const val DEFAULT_BASE_URL = "https://api.solunar.org"
    }

    override suspend fun getSolunar(
        latitude: Double,
        longitude: Double,
        date: String,
        timeZone: Int
    ): Result<Solunar> {
        analyticsTracker.logEvent(AnalyticsEvent.GetSolunar)
        return safeApiCall(dispatcher) {
            httpClient.get("$baseUrl/solunar/$latitude,$longitude,$date,$timeZone")
                .body()
        }
    }
}
