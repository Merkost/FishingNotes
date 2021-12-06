package com.joesemper.fishing.utils.time

interface TimeManager {
    fun set12hTimeFormat(isSet: Boolean)
    fun getTime(time: Long): String
    fun getDate(time: Long): String
    fun getDateTextMonth(time: Long): String
    fun getDayOfWeekAndDate(time: Long): String
    fun getDayOfWeek(time: Long): String
    fun getHours(ms: Long): String
    fun calculateDaylightHours(sunrise: Long, sunset: Long): String
    fun calculateDaylightMinutes(sunrise: Long, sunset: Long): String
    fun getDateAndTime(time: Long): String
}