package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository

class GetUserCatchesByMarkerId(private val catchesRepository: CatchesRepository) {

    suspend operator fun invoke(markerId: String) = catchesRepository.getCatchesByMarkerId(markerId)

}