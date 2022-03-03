package com.mobileprism.fishing.model.use_cases

import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.flow

class GetUserPlacesUseCase(private val repository: MarkersRepository) {

    suspend operator fun invoke() = flow {
        repository.getAllUserMarkersList().collect { markers ->
            emit(markers as List<UserMapMarker>)
        }
    }
}