package com.mobileprism.fishing.model.datasource

import androidx.core.os.LocaleListCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.ui.viewstates.ErrorType
import com.mobileprism.fishing.model.api.FreeWeatherApiService
import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.model.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class FreeWeatherRepositoryImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FreeWeatherRepository {

    companion object {
        private const val FREE_WEATHER_URL = "https://weather-by-api-ninjas.p.rapidapi.com/"

        private fun getService(): FreeWeatherApiService {
            return createRetrofit().create(FreeWeatherApiService::class.java)
        }

        private fun createRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(FREE_WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(createOkHttpClient())
                .build()
        }

        private fun createOkHttpClient(): OkHttpClient {
            val httpClient = OkHttpClient.Builder()
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClient.interceptors().add(interceptor)
            httpClient.addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.header("x-rapidapi-host", "weather-by-api-ninjas.p.rapidapi.com")
                builder.header(
                    "x-rapidapi-key",
                    "00952e723emshfbe5254b88d96a6p1c8539jsn92c8c9ef59a1"
                )
                return@addInterceptor chain.proceed(builder.build())
            }
            return httpClient.build()
        }
    }

    override suspend fun getCurrentWeatherFree(
        lat: Double,
        lon: Double
    ): Flow<Result<CurrentWeatherFree>> = flow {
        emit(safeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("get_free_weather", null)
            getService().getFreeWeather(latitude = lat, longitude = lon)
        })
    }

}