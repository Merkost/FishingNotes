package com.mobileprism.fishing.utils.time

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
