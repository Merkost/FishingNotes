package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.api.WeatherApiForecastService
import com.mobileprism.fishing.model.mappers.convertWeatherApiForecast
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherApiRepository(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WeatherRepository {

    companion object {
        private const val BASE_WEATHER_API_URL = "https://api.weatherapi.com/v1/"

        private fun getService(): WeatherApiForecastService {
            return createRetrofit().create(WeatherApiForecastService::class.java)
        }

        private fun createRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_WEATHER_API_URL)
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

            val weather = getService().getWeather(coordinates = "$lat,$lon")

            val result = convertWeatherApiForecast(weather)

            result
        }
        )
    }

    override suspend fun getHistoricalWeather(
        lat: Double,
        lon: Double,
        date: Long
    ): Flow<Result<WeatherForecast>> {
        TODO("Not yet implemented")
    }
}