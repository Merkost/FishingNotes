package com.mobileprism.fishing.ui.use_cases

import com.mobileprism.fishing.model.entity.common.ContentState
import com.mobileprism.fishing.model.entity.common.fold
import com.mobileprism.fishing.model.entity.content.MapMarker
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.*

class GetUserPlacesUseCase(private val repository: MarkersRepository) {

    suspend operator fun invoke() = channelFlow<ContentState<MapMarker>> {
        repository.getAllUserMarkers().collectLatest {
            send(it)
        }
    }
}