package com.mobileprism.fishing.model.mappers

import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.utils.getCurrentUserId
import com.mobileprism.fishing.utils.getNewMarkerId
import java.util.*

class MapMarkerMapper {

    fun mapRawMapMarker(newMarker: RawMapMarker): UserMapMarker {
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