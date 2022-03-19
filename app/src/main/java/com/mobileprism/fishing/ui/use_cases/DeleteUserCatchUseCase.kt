package com.mobileprism.fishing.ui.use_cases

import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.app.catches.CatchesRepository

class DeleteUserCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val photosRepository: PhotoStorage
) {
    suspend operator fun invoke(catch: UserCatch) {
        catchesRepository.deleteCatch(catch)
        catch.downloadPhotoLinks.forEach { photosRepository.deletePhoto(it) }
    }
}