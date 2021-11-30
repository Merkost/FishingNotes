package com.joesemper.fishing.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.compose.ui.home.map.DEFAULT_ZOOM
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

const val MILLISECONDS_IN_DAY = 86400000L
const val SECONDS_IN_DAY = 86400L
const val MILLISECONDS_IN_SECOND = 1000L
const val MOON_PHASE_INCREMENT_IN_DAY = 0.03f

fun getNewCatchId() = getRandomString(10)
fun getNewMarkerId() = getRandomString(15)
fun getNewPhotoId() = getRandomString(12)

fun getUUID() = UUID.randomUUID().toString()

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun Float.roundTo(numFractionDigits: Int): Float {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor.toFloat()
}

fun getDateBySeconds(ms: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.US)
    val date = Date(ms * 1000)
    return sdf.format(date)
}

fun getTimeBySeconds(ms: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    val date = Date(ms * 1000)
    return sdf.format(date)
}

fun getDateBySecondsTextMonth(ms: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val date = Date(ms * 1000)
    return sdf.format(date)
}

fun getDayOfWeekBySeconds(ms: Long): String {
    val sdf = SimpleDateFormat("EEE", Locale.US)
    val date = Date(ms * 1000)
    return sdf.format(date)
}

fun getHoursByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("HH", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun getDateByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun getDateByMillisecondsTextMonth(ms: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun getDayByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("dd", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun calcMoonPhase(currentPhase: Float, currentDate: Long, requiredDate: Long): Float {
    var result = currentPhase
    val dif = currentDate - requiredDate
    val numOfDays = dif / SECONDS_IN_DAY
    result -= (numOfDays * MOON_PHASE_INCREMENT_IN_DAY)
    if (result < 0.0f) {
        result += 1.0f
    }
    return result
}

fun get24hTimeByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun get12hTimeByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("hh:mm aa", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun getDateAnd24hTimeByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yy HH:mm", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun getDateAnd12hTimeByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yy hh:mm aa", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun hPaToMmHg(pressure: Int): Int {
    return (pressure * 0.75006375541921).toInt()
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun readBytes(context: Context, uri: Uri): ByteArray? =
    context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }

fun getCameraPosition(latLng: LatLng): Pair<LatLng, Float> {
    val lat = latLng.latitude + ((-100..100).random() * 0.000000001)
    val lng = latLng.longitude + ((-100..100).random() * 0.000000001)
    return Pair(LatLng(lat, lng), DEFAULT_ZOOM)
}


