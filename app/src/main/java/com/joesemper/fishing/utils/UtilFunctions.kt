package com.joesemper.fishing.utils

import java.text.SimpleDateFormat
import java.util.*


fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getNewCatchId() = getRandomString(10)
fun getNewMarkerId() = getRandomString(15)
fun getNewPhotoId() = getRandomString(12)

fun getTimeStamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

fun Double.format(digits: Int) = "%.${digits}f".format(this)