package com.joesemper.fishing.model.mappers

import com.joesemper.fishing.model.entity.common.Note
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.utils.getCurrentUserId
import com.joesemper.fishing.utils.getNewMarkerId
import com.joesemper.fishing.utils.getNewMarkerNoteId
import java.util.*

class MarkerNoteMapper {

    fun mapRawMarkerNote(newNote: Note): Note {
        return Note(
            id = getNewMarkerNoteId(),
            title = newNote.title,
            description = newNote.description,
            dateCreated = Date().time
        )
    }
}