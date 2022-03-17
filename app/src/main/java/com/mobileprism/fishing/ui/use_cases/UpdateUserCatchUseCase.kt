package com.mobileprism.fishing.domain.use_cases

import androidx.core.net.toUri
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.repository.app.CatchesRepository


class UpdateUserCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val savePhotos: SavePhotosUseCase,
) {
    suspend operator fun invoke(newCatch: UserCatch) {
        catchesRepository.updateUserCatch(
            markerId = newCatch.userMarkerId,
            catchId = newCatch.id,
            data = mapOf(
                "downloadPhotoLinks" to savePhotos(newCatch.downloadPhotoLinks.map { it.toUri() }),
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