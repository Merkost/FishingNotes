package com.mobileprism.fishing.model.datasource

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.model.api.FreeWeatherApiService
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

class FreeWeatherRepositoryImpl(
    private val analyticsTracker: AnalyticsTracker,
    private val rapidApiKey: String,
    okHttpClient: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FreeWeatherRepository {

    companion object {
        private const val FREE_WEATHER_URL = "https://weather-by-api-ninjas.p.rapidapi.com/"
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val service: FreeWeatherApiService by lazy {
        val client = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("x-rapidapi-host", "weather-by-api-ninjas.p.rapidapi.com")
                    .header("x-rapidapi-key", rapidApiKey)
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(FREE_WEATHER_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
            .create(FreeWeatherApiService::class.java)
    }

    override suspend fun getCurrentWeatherFree(
        lat: Double,
        lon: Double
    ): Flow<Result<CurrentWeatherFree>> = flow {
        emit(safeApiCall(dispatcher) {
            analyticsTracker.logEvent(AnalyticsEvent.GetFreeWeather)
            service.getFreeWeather(latitude = lat, longitude = lon)
        })
    }

}
