package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate

class DeleteUserCatchUseCase(
    private val catchesRepository: CatchesRepositoryUpdate,
    private val photosRepository: PhotoStorage
) {
    suspend operator fun invoke(catch: UserCatch): Result<Unit> {
        val result = catchesRepository.deleteCatch(catch)
        if (result.isFailure) return result
        catch.downloadPhotoLinks.forEach { photosRepository.deletePhoto(it) }
        return Result.success(Unit)
    }
}
