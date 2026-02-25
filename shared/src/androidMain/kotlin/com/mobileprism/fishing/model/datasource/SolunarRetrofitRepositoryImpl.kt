package com.mobileprism.fishing.model.datasource

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.entity.solunar.Solunar
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.model.api.SolunarApiService
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class SolunarRetrofitRepositoryImpl(
    private val analyticsTracker: AnalyticsTracker,
    okHttpClient: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SolunarRepository {

    companion object {
        private const val BASE_SOLUNAR_URL = "https://api.solunar.org/"
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val service: SolunarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_SOLUNAR_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
            .create(SolunarApiService::class.java)
    }

    override fun getSolunar(latitude: Double, longitude: Double, date: String, timeZone: Int): Flow<Result<Solunar>> =
        flow {

            analyticsTracker.logEvent(AnalyticsEvent.GetSolunar)

            emit(safeApiCall(dispatcher) {

                service.getSolunar(latitude = latitude, longitude = longitude, date = date, tz = timeZone)

            })

        }

}
