package com.joesemper.fishing.utils

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.libraries.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.joesemper.fishing.compose.ui.home.map.DEFAULT_ZOOM
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

const val MILLISECONDS_IN_DAY = 86400000L
const val SECONDS_IN_DAY = 86400L
const val MILLISECONDS_IN_SECOND = 1000L
const val MOON_PHASE_INCREMENT_IN_DAY = 0.03f

fun getNewCatchId() = getUUID()
fun getNewMarkerId() = getUUID()
fun getNewPhotoId() = getUUID()

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

fun getTimeByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    val date = Date(ms)
    return sdf.format(date)
}

fun getDateAndTimeByMilliseconds(ms: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yy HH:mm", Locale.US)
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


