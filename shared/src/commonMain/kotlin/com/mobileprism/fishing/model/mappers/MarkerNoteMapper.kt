package com.mobileprism.fishing.model.mappers

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.utils.getNewMarkerNoteId
import kotlinx.datetime.Clock

class MarkerNoteMapper {

    fun mapRawMarkerNote(newNote: Note): Note {
        return Note(
            id = getNewMarkerNoteId(),
            title = newNote.title,
            description = newNote.description,
            dateCreated = Clock.System.now().toEpochMilliseconds()
        )
    }
}
