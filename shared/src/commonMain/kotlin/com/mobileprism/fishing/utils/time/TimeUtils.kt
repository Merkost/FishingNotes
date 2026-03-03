package com.mobileprism.fishing.utils.time

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object TimeConstants {
    const val MILLISECONDS_IN_DAY = 86400000L
    const val SECONDS_IN_DAY = 86400L
    const val MILLISECONDS_IN_SECOND = 1000L
    const val MILLISECONDS_IN_HOUR = 3600000L
    const val SECONDS_IN_HOUR = 3600L
    const val SECONDS_IN_MINUTE = 60L
    const val MOON_PHASE_INCREMENT_IN_DAY = 0.0295305882f
    const val MOON_ZERO_DATE_SECONDS = 1643705100L
}

fun Long.hoursCount(): Int {
    return (formatToMilliseconds(this) / TimeConstants.MILLISECONDS_IN_HOUR).toInt()
}

fun Long.daysCount(): Int {
    return (formatToMilliseconds(this) / TimeConstants.MILLISECONDS_IN_DAY).toInt()
}

fun formatToMilliseconds(time: Long): Long {
    return if (time > 1000000000000) time else time * TimeConstants.MILLISECONDS_IN_SECOND
}

fun calculateDaylightTime(sunrise: Long, sunset: Long, hoursLabel: String, minutesLabel: String): String {
    val daylightTime = sunset - sunrise
    val hours = getHoursBySeconds(daylightTime)
    val minutes = getMinutesBySeconds(daylightTime)

    return "$hours $hoursLabel $minutes $minutesLabel"
}

private fun getHoursBySeconds(s: Long): String {
    return (s / TimeConstants.SECONDS_IN_HOUR).toString()
}

private fun getMinutesBySeconds(s: Long): String {
    return ((s % TimeConstants.SECONDS_IN_HOUR) / TimeConstants.SECONDS_IN_MINUTE).toString()
}

private val shortDayNames = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val shortMonthNames = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun toLocalDateTime(millis: Long): kotlinx.datetime.LocalDateTime {
    val ms = formatToMilliseconds(millis)
    return kotlin.time.Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault())
}

fun Long.toTime(is12hFormat: Boolean = false): String {
    val dt = toLocalDateTime(this)
    return if (is12hFormat) {
        val hour12 = if (dt.hour % 12 == 0) 12 else dt.hour % 12
        val amPm = if (dt.hour < 12) "AM" else "PM"
        "${hour12.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')} $amPm"
    } else {
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    }
}

fun Long.toDate(): String {
    val dt = toLocalDateTime(this)
    val monthNum = dt.month.ordinal + 1
    return "${dt.day.toString().padStart(2, '0')}.${monthNum.toString().padStart(2, '0')}.${dt.year}"
}

fun Long.toDateTextMonth(): String {
    val dt = toLocalDateTime(this)
    val month = shortMonthNames[dt.month.ordinal]
    return "${dt.day.toString().padStart(2, '0')} $month ${dt.year}"
}

fun Long.toDayOfWeek(): String {
    val dt = toLocalDateTime(this)
    return shortDayNames[dt.dayOfWeek.ordinal].uppercase()
}

fun Long.toDayOfWeekAndDate(): String {
    val dt = toLocalDateTime(this)
    val day = shortDayNames[dt.dayOfWeek.ordinal].uppercase()
    return "$day ${dt.day.toString().padStart(2, '0')}"
}

fun Long.toHours(): String {
    val dt = toLocalDateTime(this)
    return dt.hour.toString().padStart(2, '0')
}
