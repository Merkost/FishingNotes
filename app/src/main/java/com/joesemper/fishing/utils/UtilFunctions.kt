package com.joesemper.fishing.utils

import java.text.SimpleDateFormat
import java.util.*


fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getTimeStamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())