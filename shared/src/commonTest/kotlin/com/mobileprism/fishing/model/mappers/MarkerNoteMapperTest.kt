package com.mobileprism.fishing.model.mappers

import com.mobileprism.fishing.domain.entity.common.Note
import kotlin.time.Clock
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkerNoteMapperTest {

    private val mapper = MarkerNoteMapper()

    @Test
    fun generatesNonEmptyIdOfLength6() {
        val input = Note(id = "", title = "Title", description = "Desc", dateCreated = 0L)

        val result = mapper.mapRawMarkerNote(input)

        assertEquals(6, result.id.length)
        assertTrue(result.id.isNotEmpty())
    }

    @Test
    fun preservesTitleAndDescriptionFromInput() {
        val input = Note(id = "ignored", title = "My Title", description = "My Description", dateCreated = 0L)

        val result = mapper.mapRawMarkerNote(input)

        assertEquals("My Title", result.title)
        assertEquals("My Description", result.description)
    }

    @Test
    fun setsDateCreatedToApproximatelyCurrentTime() {
        val beforeMillis = Clock.System.now().toEpochMilliseconds()
        val input = Note(id = "", title = "T", description = "D", dateCreated = 0L)

        val result = mapper.mapRawMarkerNote(input)

        val afterMillis = Clock.System.now().toEpochMilliseconds()

        assertTrue(
            result.dateCreated in beforeMillis..afterMillis,
            "dateCreated (${result.dateCreated}) should be between $beforeMillis and $afterMillis"
        )
    }
}
