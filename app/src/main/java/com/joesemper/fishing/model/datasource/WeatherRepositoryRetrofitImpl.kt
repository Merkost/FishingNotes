package com.joesemper.fishing.model.datasource

import androidx.core.os.LocaleListCompat
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.joesemper.fishing.domain.viewstates.ResultWrapper
import com.joesemper.fishing.model.api.WeatherApiService
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Error

class WeatherRepositoryRetrofitImpl : WeatherRepository {

    val locale = LocaleListCompat.getAdjustedDefault().toLanguageTags().take(2)

    companion object {
        private const val BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
    }

    override fun getWeather(lat: Double, lon: Double)
    : Flow<ResultWrapper<WeatherForecast>> = flow {
        try {
            val weather = getService().getWeather(latitude = lat, longitude = lon,
                lang = locale)
            emit(ResultWrapper.Success(weather))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e))
        }

    }

    override suspend fun getHistoricalWeather(lat: Double, lon: Double, date: Long)
    : Flow<ResultWrapper<WeatherForecast>> = flow {
        try {
            val weather = getService().getHistoricalWeather(latitude = lat, longitude = lon, dt = date,
                lang = locale)
            emit(ResultWrapper.Success(weather))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e))
        }
    }


    override suspend fun getWeatherForecast(lat: Double, lon: Double) =
        getService().getWeather(latitude = lat, longitude = lon)

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