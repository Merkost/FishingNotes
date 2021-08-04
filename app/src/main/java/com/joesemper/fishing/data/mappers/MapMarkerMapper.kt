package com.joesemper.fishing.data.mappers

import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.entity.content.UserMapMarker
import com.joesemper.fishing.utils.getCurrentUserId
import com.joesemper.fishing.utils.getNewMarkerId

class MapMarkerMapper {

    fun mapRawMapMarker(newMarker: RawMapMarker): UserMapMarker {
        return UserMapMarker(
            id = getNewMarkerId(),
            userId = getCurrentUserId(),
            latitude = newMarker.latitude.toDouble(),
            longitude = newMarker.longitude.toDouble(),
            title = newMarker.title,
            description = newMarker.description,
            isPublic = newMarker.isPublic
        )
    }
}