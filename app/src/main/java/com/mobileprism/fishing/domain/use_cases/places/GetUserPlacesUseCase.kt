package com.mobileprism.fishing.domain.use_cases.places

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.fold
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import kotlinx.coroutines.flow.channelFlow

class GetUserPlacesUseCase(private val repository: MarkersRepository) {

    suspend operator fun invoke() = channelFlow<ContentState<MapMarker>> {
        repository.getAllUserMarkers().collect {
            it.fold(
                onAdded = {

                },
                onDeleted = {

                },
                onModified = {

                }
            )
        }
    }
}
// TODO: Del