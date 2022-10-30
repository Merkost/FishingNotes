package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteConfirm
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteFind
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteReset
import com.mobileprism.fishing.model.entity.FishingResponse
import com.mobileprism.fishing.model.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface RestoreRepository {
    suspend fun searchAccount(find: RestoreRemoteFind): Flow<ResultWrapper<FishingResponse>>
    suspend fun confirmOTP(confirm: RestoreRemoteConfirm): Flow<ResultWrapper<FishingResponse>>
    suspend fun restorePassword(reset: RestoreRemoteReset): Flow<ResultWrapper<FishingResponse>>
}
