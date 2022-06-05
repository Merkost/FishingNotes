package com.mobileprism.fishing.model.datasource.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobileprism.fishing.domain.entity.common.Note
import java.lang.reflect.Type

object Converters {

        @TypeConverter
        fun toNotes(notesString: String?): List<Note> {
            if (notesString == null) {
                return listOf()
            }
            val gson = Gson()
            val type: Type = object : TypeToken<List<Note>?>() {}.type
            return gson.fromJson(notesString, type)
        }

        @TypeConverter
        fun toString(notes: List<Note>): String {
            val gson = Gson()
            return gson.toJson(notes)
        }


    @TypeConverter
    fun toNote(string: String?): Note? {
        if (string == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Note?>() {}.type
        return gson.fromJson(string, type)
    }

    @TypeConverter
    fun noteToString(note: Note): String {
        val gson = Gson()
        return gson.toJson(note)
    }



}