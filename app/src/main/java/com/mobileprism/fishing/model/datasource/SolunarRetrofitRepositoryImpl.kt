package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.ui.viewstates.ErrorType
import com.mobileprism.fishing.model.api.SolunarApiService
import com.mobileprism.fishing.model.entity.solunar.Solunar
import com.mobileprism.fishing.model.repository.app.SolunarRepository
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.logging.HttpLoggingInterceptor


class SolunarRetrofitRepositoryImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SolunarRepository {

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

    override fun getSolunar(latitude: Double, longitude: Double, date: String, timeZone: Int): Flow<Result<Solunar>> =
        flow {

            firebaseAnalytics.logEvent("get_solunar", null)

            emit(safeApiCall(dispatcher) {

                getService().getSolunar(latitude = latitude, longitude = longitude, date = date, tz = timeZone)

            })

        }

}