package com.mobileprism.fishing.model.datasource.fake

import com.mobileprism.fishing.domain.repository.app.FishRepository
import com.mobileprism.fishing.model.entity.FishResponse
import com.mobileprism.fishing.model.entity.FishTypeResponse
import com.mobileprism.fishing.model.utils.safeApiCall
import com.mobileprism.fishing.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class FakeFishRepositoryRetrofitImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FishRepository {

    override suspend fun updateFish(): Result<FishResponse> =
        safeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)
            FishResponse(
                listOf<FishTypeResponse>(
                    FishTypeResponse(1, "lat1", "eng1", "rus1"),
                    FishTypeResponse(2, "lat2", "eng2", "rus2"),
                    FishTypeResponse(3, "lat3", "eng3", "rus3"),
                )
            )

        }

}