package com.joesemper.fishing.utils.time

import android.content.Context
import com.joesemper.fishing.R
import com.joesemper.fishing.utils.MILLISECONDS_IN_SECOND
import java.text.SimpleDateFormat
import java.util.*

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
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    return sdf.format(date)
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

fun calculateDaylightTime(context: Context, sunrise: Long, sunset: Long): String {
    val daylightTime = sunset - sunrise
    val hours = getHoursBySeconds(daylightTime)
    val minutes = getMinutesBySeconds(daylightTime)

    return "$hours ${context.getString(R.string.hours)} $minutes ${context.getString(R.string.minutes)}"
}

private fun formatToMilliseconds(time: Long): Long {
    return if (time > 1000000000000) time else time * MILLISECONDS_IN_SECOND
}

private fun getHoursBySeconds(s: Long): String {
    return (s / com.joesemper.fishing.utils.SECONDS_IN_HOUR).toString()
}

private fun getMinutesBySeconds(s: Long): String {
    return ((s % com.joesemper.fishing.utils.SECONDS_IN_HOUR) / com.joesemper.fishing.utils.SECONDS_IN_MINUTE).toString()
}
