package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.MarkersRepository

class GetMapMarkerByIdUseCase(
    private val markersRepository: MarkersRepository,
) {
    suspend operator fun invoke(markerId: String): Result<UserMapMarker> {
        return markersRepository.getMapMarker(markerId)
    }
}