package com.mobileprism.fishing.domain.use_cases.catches

import androidx.core.net.toUri
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase


class UpdateUserCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val savePhotos: SavePhotosUseCase,
) {
    suspend operator fun invoke(newCatch: UserCatch) {
        catchesRepository.updateUserCatch(newCatch)
    }
}