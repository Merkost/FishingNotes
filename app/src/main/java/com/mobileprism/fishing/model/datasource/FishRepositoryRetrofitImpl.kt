package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.repository.app.FishRepository
import com.mobileprism.fishing.model.api.FishApiService
import com.mobileprism.fishing.model.entity.FishResponse
import com.mobileprism.fishing.model.entity.FishTypeResponse
import com.mobileprism.fishing.model.utils.safeApiCall
import com.mobileprism.fishing.utils.Constants.API_URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FishRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val okHttpClient: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FishRepository {

    private fun getService(): FishApiService {
        return createRetrofit().create(FishApiService::class.java)
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    override suspend fun updateFish(): Result<FishResponse> =
        safeApiCall(dispatcher) {
            //getService().syncFishInfo()

            FishResponse(
                listOf<FishTypeResponse>(
                    FishTypeResponse(1, "lat1", "eng1", "rus1"),
                    FishTypeResponse(2, "lat2", "eng2", "rus2"),
                    FishTypeResponse(3, "lat3", "eng3", "rus3"),
                )
            )

        }

}