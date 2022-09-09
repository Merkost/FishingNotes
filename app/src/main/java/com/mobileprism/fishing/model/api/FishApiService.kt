package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.model.entity.FishResponse
import retrofit2.http.GET

interface FishApiService {

    @GET("fish")
    suspend fun syncFishInfo(): FishResponse

}