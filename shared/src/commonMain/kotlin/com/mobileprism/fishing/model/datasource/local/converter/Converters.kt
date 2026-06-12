package com.mobileprism.fishing.model.datasource.local.converter

import androidx.room.TypeConverter
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return try { Json.decodeFromString<List<String>>(value) } catch (_: Exception) { emptyList() }
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun fromNoteList(value: String): List<Note> {
        return try { Json.decodeFromString<List<Note>>(value) } catch (_: Exception) { emptyList() }
    }

    @TypeConverter
    fun toNoteList(list: List<Note>): String {
        return Json.encodeToString(list)
    }
}
