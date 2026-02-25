package com.mobileprism.fishing.model.mappers

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.utils.getNewMarkerNoteId
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