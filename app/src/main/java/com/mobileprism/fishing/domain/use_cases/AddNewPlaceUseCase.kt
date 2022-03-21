package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single

class AddNewPlaceUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        newMarker: RawMapMarker
    ) = flow {
        emit(markersRepository.addNewMarker(newMarker).single())
    }
}
