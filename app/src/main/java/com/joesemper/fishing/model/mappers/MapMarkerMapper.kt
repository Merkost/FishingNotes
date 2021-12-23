package com.joesemper.fishing.model.mappers

import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.utils.getCurrentUserId
import com.joesemper.fishing.utils.getNewMarkerId
import java.util.*

class MapMarkerMapper {

    fun mapRawMapMarker(newMarker: RawMapMarker): UserMapMarker {
        return UserMapMarker(
            id = getNewMarkerId(),
            userId = getCurrentUserId(),
            latitude = newMarker.latitude.toDouble(),
            longitude = newMarker.longitude.toDouble(),
            title = newMarker.title,
            description = newMarker.description,
            markerColor = newMarker.markerColor,
            public = newMarker.public,
            visible = newMarker.visible,
            dateOfCreation = Date().time
        )
    }
}