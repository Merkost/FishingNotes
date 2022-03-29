package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import kotlinx.coroutines.flow.flow

class GetUserPlacesListUseCase(private val repository: MarkersRepository) {

    operator fun invoke() = flow {
        repository.getAllUserMarkersList().collect { markers ->
            emit(markers as List<UserMapMarker>)
        }
    }
}