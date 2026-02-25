package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate

class SubscribeOnUserCatchStateUseCase(
    private val catchesRepository: CatchesRepositoryUpdate,
) {
    operator fun invoke(markerId: String, catchId: String) =
        catchesRepository.subscribeOnUserCatchState(markerId = markerId, catchId = catchId)
}
