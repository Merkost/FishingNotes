package com.mobileprism.fishing.model.api.fishing

import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteConfirm
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteFind
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteReset
import com.mobileprism.fishing.model.entity.FishingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RestoreApiService {

    @POST("restore/search")
    suspend fun searchAccount(
        @Body body: RestoreRemoteFind
    ): Response<FishingResponse>

    @POST("restore/confirm")
    suspend fun confirmOTP(
        @Body body: RestoreRemoteConfirm
    ): Response<FishingResponse>

    @POST("restore/reset")
    suspend fun restorePassword(
        @Body body: RestoreRemoteReset
    ): Response<FishingResponse>

}