package com.joesemper.fishing.utils.time

import android.content.Context
import com.joesemper.fishing.R
import com.joesemper.fishing.utils.MILLISECONDS_IN_SECOND
import java.text.SimpleDateFormat
import java.util.*


//class TimeManagerImpl(
//    val preferences: UserPreferences
//) : TimeManager {
//
//    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//
//    init {
//        subscribeOnSettings()
//    }
//
//    private fun subscribeOnSettings() {
//        applicationScope.launch {
//            preferences.use12hTimeFormat.collect {
//                is12HoursFormat = it
//            }
//        }
//    }
//
//    private var is12HoursFormat = false

//    override fun getTime(time: Long): String {
//        val date = Date(formatToMilliseconds(time))
////        val sdf = if (is12HoursFormat) {
////            SimpleDateFormat("hh:mm aa", Locale.US)
////        } else {
////            SimpleDateFormat("HH:mm", Locale.US)
////        }
////        return sdf.format(date)
////    }
////
////    override fun getDate(time: Long): String {
////        val date = Date(formatToMilliseconds(time))
////        val sdf = SimpleDateFormat("dd.MM.yy", Locale.US)
////        return sdf.format(date)
////    }
////
////    override fun getDateTextMonth(time: Long): String {
////        val date = Date(formatToMilliseconds(time))
////        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
////        return sdf.format(date)
////    }
//
////    override fun getDayOfWeekAndDate(time: Long): String {
////        val date = Date(formatToMilliseconds(time))
////        val sdf = SimpleDateFormat("EEE dd", Locale.US)
////        return sdf.format(date)
////    }
//
////    override fun getDayOfWeek(time: Long): String {
////        val date = Date(formatToMilliseconds(time))
////        val sdf = SimpleDateFormat("EEE", Locale.US)
////        return sdf.format(date)
////    }
//
////    override fun getHours(ms: Long): String {
////        val sdf = SimpleDateFormat("HH", Locale.US)
////        val date = Date(ms)
////        return sdf.format(date)
////    }
//
//    override fun calculateDaylightHours(sunrise: Long, sunset: Long): String {
//        val daylightTime = sunset - sunrise
//        return getHoursBySeconds(daylightTime)
//    }
//
//    override fun calculateDaylightMinutes(sunrise: Long, sunset: Long): String {
//        val daylightTime = sunset - sunrise
//        return getMinutesBySeconds(daylightTime)
//    }
//
////    override fun getDateAndTime(time: Long): String {
////        val date = Date(formatToMilliseconds(time))
////        val sdf = if (is12HoursFormat) {
////            SimpleDateFormat("dd.MM.yy hh:mm aa", Locale.US)
////        } else {
////            SimpleDateFormat("dd.MM.yy HH:mm", Locale.US)
////        }
////        return sdf.format(date)
////    }
//
//}

fun Long.toTime(is12hFormat: Boolean = false): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = if (is12hFormat) {
        SimpleDateFormat("hh:mm aa", Locale.US)
    } else {
        SimpleDateFormat("HH:mm", Locale.US)
    }
    return sdf.format(date)
}

fun Long.toDate(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.US)
    return sdf.format(date)
}

fun Long.toDateTextMonth(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(date)
}

fun Long.toDayOfWeek(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("EEE", Locale.US)
    return sdf.format(date)
}

fun Long.toDayOfWeekAndDate(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("EEE dd", Locale.US)
    return sdf.format(date)
}

fun Long.toHours(): String {
    val date = Date(formatToMilliseconds(this))
    val sdf = SimpleDateFormat("HH", Locale.US)
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