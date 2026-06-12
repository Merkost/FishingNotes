package com.mobileprism.fishing.model.datasource.local.converter

import com.mobileprism.fishing.domain.entity.common.Note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConvertersTest {

    private val converters = Converters()

    // ---- String list ----

    @Test
    fun stringListRoundTrip() {
        val original = listOf("alpha", "beta", "gamma")

        val json = converters.toStringList(original)
        val restored = converters.fromStringList(json)

        assertEquals(original, restored)
    }

    @Test
    fun fromStringListWithEmptyJsonArrayReturnsEmptyList() {
        val result = converters.fromStringList("[]")

        assertTrue(result.isEmpty())
    }

    @Test
    fun fromStringListWithCorruptJsonReturnsEmptyList() {
        val result = converters.fromStringList("not a json array!!!")

        assertTrue(result.isEmpty())
    }

    // ---- Note list ----

    @Test
    fun noteListRoundTrip() {
        val original = listOf(
            Note(id = "n1", title = "Title 1", description = "Desc 1", dateCreated = 1000L),
            Note(id = "n2", title = "Title 2", description = "Desc 2", dateCreated = 2000L),
        )

        val json = converters.toNoteList(original)
        val restored = converters.fromNoteList(json)

        assertEquals(original, restored)
    }

    @Test
    fun fromNoteListWithEmptyJsonArrayReturnsEmptyList() {
        val result = converters.fromNoteList("[]")

        assertTrue(result.isEmpty())
    }

    @Test
    fun fromNoteListWithCorruptJsonReturnsEmptyList() {
        val result = converters.fromNoteList("{broken json]]]")

        assertTrue(result.isEmpty())
    }
}
