package com.mobileprism.fishing.model.datasource.local.converter

import androidx.room.TypeConverter
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return Json.decodeFromString<List<String>>(value)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun fromNoteList(value: String): List<Note> {
        return Json.decodeFromString<List<Note>>(value)
    }

    @TypeConverter
    fun toNoteList(list: List<Note>): String {
        return Json.encodeToString(list)
    }
}
