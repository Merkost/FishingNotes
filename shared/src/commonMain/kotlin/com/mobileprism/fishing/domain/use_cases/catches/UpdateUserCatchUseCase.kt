package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase

class UpdateUserCatchUseCase(
    private val catchesRepository: CatchesRepositoryUpdate,
    private val savePhotos: SavePhotosUseCase,
) {
    suspend operator fun invoke(newCatch: UserCatch) {
        catchesRepository.updateUserCatch(
            markerId = newCatch.userMarkerId,
            catchId = newCatch.id,
            data = mapOf(
                "downloadPhotoLinks" to savePhotos(newCatch.downloadPhotoLinks),
                "fishType" to newCatch.fishType,
                "fishAmount" to newCatch.fishAmount,
                "fishWeight" to newCatch.fishWeight,
                "fishingRodType" to newCatch.fishingRodType,
                "fishingBait" to newCatch.fishingBait,
                "fishingLure" to newCatch.fishingLure,
                "note" to newCatch.note
            )
        )
    }
}
