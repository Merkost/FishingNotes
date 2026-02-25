package com.mobileprism.fishing.domain.use_cases.places

import androidx.compose.ui.graphics.Color
import com.firebase.ui.auth.data.model.User
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.utils.getCurrentUserId
import com.mobileprism.fishing.utils.getNewMarkerId
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.util.*

class AddNewPlaceUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        newMarker: RawMapMarker
    ) = flow {
        emit(markersRepository.addNewMarker(getMapMarker(newMarker)))
    }

    private fun getMapMarker(newMarker: RawMapMarker): UserMapMarker {
        return UserMapMarker(
            id = getNewMarkerId(),
            userId = getCurrentUserId(),
            latitude = newMarker.latitude,
            longitude = newMarker.longitude,
            title = newMarker.title,
            description = newMarker.description,
            markerColor = newMarker.markerColor,
            public = newMarker.public,
            visible = newMarker.visible,
            dateOfCreation = Date().time
        )
    }
}
