package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.Flow

class GetMapMarkerByIdUseCase(
    private val markersRepository: MarkersRepository,
) {
    operator fun invoke(markerId: String): Flow<UserMapMarker?> {
        return markersRepository.getMapMarker(markerId)
    }
}