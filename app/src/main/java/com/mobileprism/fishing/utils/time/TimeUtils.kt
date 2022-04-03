package com.mobileprism.fishing.utils.time

import android.content.Context
import com.mobileprism.fishing.R
import com.mobileprism.fishing.utils.time.TimeConstants.MILLISECONDS_IN_DAY
import com.mobileprism.fishing.utils.time.TimeConstants.MILLISECONDS_IN_HOUR
import com.mobileprism.fishing.utils.time.TimeConstants.MILLISECONDS_IN_SECOND
import com.mobileprism.fishing.utils.time.TimeConstants.SECONDS_IN_HOUR
import com.mobileprism.fishing.utils.time.TimeConstants.SECONDS_IN_MINUTE
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

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

fun Long.toTime(is12hFormat: Boolean = false): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = if (is12hFormat) {
        SimpleDateFormat("hh:mm aa", Locale.getDefault())
    } else {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    return sdf.format(date)
}

fun Long.toDate(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(date)
}

fun Long.daysCount(): Int {
    return (formatToMilliseconds(this) / MILLISECONDS_IN_DAY).toInt()
}

fun Long.toDateTextMonth(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(date)
}

fun Long.toDayOfWeek(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    return sdf.format(date).onEach { it.uppercase() }
}

fun Long.toDayOfWeekAndDate(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("EEE dd", Locale.getDefault())
    return sdf.format(date).onEach { if (!it.isDigit()) it.uppercase() }
}

fun Long.toHours(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("HH", Locale.getDefault())
    return sdf.format(date)
}

fun Long.hoursCount(): Int {
    return (formatToMilliseconds(this) / MILLISECONDS_IN_HOUR).toInt()
}

fun calculateDaylightTime(context: Context, sunrise: Long, sunset: Long): String {
    val daylightTime = sunset - sunrise
    val hours = getHoursBySeconds(daylightTime)
    val minutes = getMinutesBySeconds(daylightTime)

    return "$hours ${context.getString(R.string.hours)} $minutes ${context.getString(R.string.minutes)}"
}

fun formatToMilliseconds(time: Long): Long {
    return if (time > 1000000000000) time else time * MILLISECONDS_IN_SECOND
}

private fun getHoursBySeconds(s: Long): String {
    return (s / SECONDS_IN_HOUR).toString()
}

private fun getMinutesBySeconds(s: Long): String {
    return ((s % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE).toString()
}
