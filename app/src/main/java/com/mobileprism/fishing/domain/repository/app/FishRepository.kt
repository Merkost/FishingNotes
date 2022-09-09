package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.model.entity.FishResponse

interface FishRepository {
    suspend fun updateFish(): Result<FishResponse>

}