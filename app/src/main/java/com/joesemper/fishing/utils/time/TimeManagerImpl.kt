package com.joesemper.fishing.utils.time

import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.utils.MILLISECONDS_IN_SECOND
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class TimeManagerImpl(
    val preferences: UserPreferences
) : TimeManager {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        subscribeOnSettings()
    }

    private fun subscribeOnSettings() {
        applicationScope.launch {
            preferences.use12hTimeFormat.collect {
                is12HoursFormat = it
            }
        }

    }

    private var is12HoursFormat = false

    override fun set12hTimeFormat(isSet: Boolean) {
        is12HoursFormat = isSet
    }

    override fun getTime(time: Long): String {
        val date = Date(formatToMilliseconds(time))
        val sdf = if (is12HoursFormat) {
            SimpleDateFormat("hh:mm aa", Locale.US)
        } else {
            SimpleDateFormat("HH:mm", Locale.US)
        }
        return sdf.format(date)
    }

    override fun getDate(time: Long): String {
        val date = Date(formatToMilliseconds(time))
        val sdf = SimpleDateFormat("dd.MM.yy", Locale.US)
        return sdf.format(date)
    }

    override fun getDateTextMonth(time: Long): String {
        val date = Date(formatToMilliseconds(time))
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return sdf.format(date)
    }

    override fun getDayOfWeekAndDate(time: Long): String {
        val date = Date(formatToMilliseconds(time))
        val sdf = SimpleDateFormat("EEE dd", Locale.US)
        return sdf.format(date)
    }

    override fun getDayOfWeek(time: Long): String {
        val date = Date(formatToMilliseconds(time))
        val sdf = SimpleDateFormat("EEE", Locale.US)
        return sdf.format(date)
    }

    override fun getHours(ms: Long): String {
        val sdf = SimpleDateFormat("HH", Locale.US)
        val date = Date(ms)
        return sdf.format(date)
    }

    override fun calculateDaylightHours(sunrise: Long, sunset: Long): String {
        val daylightTime = sunset - sunrise
        return getHoursBySeconds(daylightTime)
    }

    override fun calculateDaylightMinutes(sunrise: Long, sunset: Long): String {
        val daylightTime = sunset - sunrise
        return getMinutesBySeconds(daylightTime)
    }

    override fun getDateAndTime(time: Long): String {
        val date = Date(formatToMilliseconds(time))
        val sdf = if (is12HoursFormat) {
            SimpleDateFormat("dd.MM.yy hh:mm aa", Locale.US)
        } else {
            SimpleDateFormat("dd.MM.yy HH:mm", Locale.US)
        }
        return sdf.format(date)
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
}