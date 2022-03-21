package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository

class SubscribeOnUserCatchStateUseCase(
    private val catchesRepository: CatchesRepository,
) {
    operator fun invoke(markerId: String, catchId: String) =
        catchesRepository.subscribeOnUserCatchState(markerId = markerId, catchId = catchId)
}