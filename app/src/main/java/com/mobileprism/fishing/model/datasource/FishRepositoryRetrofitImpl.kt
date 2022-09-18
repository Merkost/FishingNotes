package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.repository.app.FishRepository
import com.mobileprism.fishing.model.api.FishApiService
import com.mobileprism.fishing.model.entity.FishResponse
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FishRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val fishApiService: FishApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FishRepository {

    override suspend fun updateFish(): Result<FishResponse> =
        safeApiCall(dispatcher) {
            fishApiService.syncFishInfo()
        }

}