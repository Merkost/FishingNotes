package com.mobileprism.fishing.domain.use_cases.places

import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import kotlinx.coroutines.flow.flow

class GetUserPlacesListUseCase(private val repository: MarkersRepository) {

    suspend operator fun invoke() = flow {
        repository.getAllUserMarkersList().collect { markers ->
            emit(markers)
        }
    }
}