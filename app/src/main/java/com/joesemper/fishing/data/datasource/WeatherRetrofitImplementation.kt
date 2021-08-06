package com.joesemper.fishing.data.datasource

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.joesemper.fishing.data.api.WeatherApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRetrofitImplementation : WeatherProvider {

    companion object {
        private const val BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @ExperimentalCoroutinesApi
    override fun getWeather(lat: Double, lon: Double) = flow {
        val weather = getService().getWeather(latitude = lat, longitude = lon)
        emit(weather)
    }

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