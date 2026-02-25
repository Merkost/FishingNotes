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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SolunarRepositoryKtorImpl(
    private val analyticsTracker: AnalyticsTracker,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SolunarRepository {

    companion object {
        private const val BASE_URL = "https://api.solunar.org"
    }

    override fun getSolunar(
        latitude: Double,
        longitude: Double,
        date: String,
        timeZone: Int
    ): Flow<Result<Solunar>> = flow {
        analyticsTracker.logEvent(AnalyticsEvent.GetSolunar)
        emit(safeApiCall(dispatcher) {
            httpClient.get("$BASE_URL/solunar/$latitude,$longitude,$date,$timeZone")
                .body()
        })
    }
}
