package com.mobileprism.fishing.model.api.fishing

import com.mobileprism.fishing.model.entity.FishResponse
import retrofit2.http.GET

interface FishApiService {

    @GET("fish")
    suspend fun syncFishInfo(): FishResponse

}