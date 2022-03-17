package com.mobileprism.fishing.model.datasource

import androidx.core.os.LocaleListCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.model.api.WeatherApiService
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okio.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WeatherRepository {

    private val locale = LocaleListCompat.getAdjustedDefault().toLanguageTags().take(2)

    companion object {
        private const val BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
        private const val FREE_WEATHER_URL = "https://weather-by-api-ninjas.p.rapidapi.com/"

        private fun getService(): WeatherApiService {
            return createRetrofit().create(WeatherApiService::class.java)
        }

        private fun createRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(createOkHttpClient())
                .build()
        }

        private fun createOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder().build()
        }
    }

    override suspend fun getWeather(lat: Double, lon: Double)
    : Flow<Result<WeatherForecast>> = flow {
        emit(safeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("get_weather", null)
            getService().getWeather(
                latitude = lat, longitude = lon,
                lang = locale
            )
        })

    }

    override suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long)
    : Flow<Result<WeatherForecast>> = flow {
        emit(safeApiCall(dispatcher) {
            getService().getHistoricalWeather(
                latitude = lat, longitude = lon, dt = date,
                lang = locale
            )
        })
    }

}