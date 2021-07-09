package com.joesemper.fishing.model.repository.weather

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.joesemper.fishing.model.repository.weather.api.WeatherApiService
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRetrofitImplementation : WeatherRepository {

    companion object {
        private const val BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
    }

    override suspend fun getData(lat: Float, lon: Float): WeatherForecast {
        return getService().getWeather(latitude = lat, longitude = lon)
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