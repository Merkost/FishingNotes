package com.joesemper.fishing.view.weather.utils

import com.joesemper.fishing.R
import java.text.SimpleDateFormat
import java.util.*

fun getWeatherIconByName(name: String): Int {
    return when(true) {
        name.startsWith("01", true) -> { R.drawable.ic_clear_sky }
        name.startsWith("02", true) -> { R.drawable.ic_few_clouds }
        name.startsWith("03", true) -> { R.drawable.ic_broken_clouds }
        name.startsWith("04", true) -> { R.drawable.ic_broken_clouds }
        name.startsWith("09", true) -> { R.drawable.ic_shower_rain }
        name.startsWith("10", true) -> { R.drawable.ic_rain }
        name.startsWith("11", true) -> { R.drawable.ic_thunderstorm }
        name.startsWith("13", true) -> { R.drawable.ic_snow }
        name.startsWith("50", true) -> { R.drawable.ic_mist }
        else -> { R.drawable.ic_clear_sky }
    }
}

fun getDateByMilliseconds(ms: Long): String {
//    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = Date(ms * 1000)
//    calendar.time = date
//    calendar.timeInMillis = ms
    return sdf.format(date)
}