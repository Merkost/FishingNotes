package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository

class DeleteUserCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val photosRepository: PhotoStorage
) {
    suspend operator fun invoke(catch: UserCatch) {
        catchesRepository.deleteCatch(catch)
        catch.downloadPhotoLinks.forEach { photosRepository.deletePhoto(it) }
    }
}