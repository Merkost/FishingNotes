package com.mobileprism.fishing.model.datasource

import androidx.core.os.LocaleListCompat
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.api.WeatherApiService
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

class WeatherRepositoryRetrofitImpl(
    private val analyticsTracker: AnalyticsTracker,
    private val openWeatherKey: String,
    private val okHttpClient: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WeatherRepository {

    private val locale = LocaleListCompat.getAdjustedDefault().toLanguageTags().take(2)

    companion object {
        private const val BASE_WEATHER_URL = "https://api.openweathermap.org/data/3.0/"
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_WEATHER_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
            .create(WeatherApiService::class.java)
    }

    override suspend fun getWeather(lat: Double, lon: Double)
    : Flow<Result<WeatherForecast>> = flow {
        emit(safeApiCall(dispatcher) {
            analyticsTracker.logEvent(AnalyticsEvent.GetWeather)
            service.getWeather(
                latitude = lat, longitude = lon,
                lang = locale,
                appid = openWeatherKey
            )
        })

    }

    override suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long)
    : Flow<Result<WeatherForecast>> = flow {
        emit(safeApiCall(dispatcher) {
            service.getHistoricalWeather(
                latitude = lat, longitude = lon, dt = date,
                lang = locale,
                appid = openWeatherKey
            )
        })
    }

}
