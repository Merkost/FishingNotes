package com.joesemper.fishing.model.datasource

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.joesemper.fishing.domain.viewstates.ErrorType
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.api.SolunarApiService
import com.joesemper.fishing.model.entity.solunar.Solunar
import com.joesemper.fishing.model.repository.app.SolunarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.logging.HttpLoggingInterceptor




class SolunarRetrofitRepositoryImpl : SolunarRepository {

    companion object {
        private const val BASE_SOLUNAR_URL = "https://api.solunar.org/"

        private fun getService(): SolunarApiService {
            return createRetrofit().create(SolunarApiService::class.java)
        }

        private fun createRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_SOLUNAR_URL)
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

            return httpClient.build()
        }
    }

    override fun getSolunar(latitude: Double, longitude: Double): Flow<RetrofitWrapper<Solunar>> =
        flow {
            try {
                val currentDate = Date()
                val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                val date = format.format(currentDate)
                val timezone = TimeZone.getDefault().getOffset(currentDate.time)

                val solunar = getService().getSolunar(
                    latitude = latitude, longitude = longitude,
                    date = date,
                    tz = timezone
                )

                emit(RetrofitWrapper.Success(solunar))
            } catch (e: IOException) {
                emit(RetrofitWrapper.Error(ErrorType.NetworkError(e)))
            } catch (e: Exception) {
                emit(RetrofitWrapper.Error(ErrorType.OtherError(e)))
            }
        }

}