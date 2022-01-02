package com.joesemper.fishing.utils

import android.content.Context
import android.widget.Toast
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.compose.ui.home.map.DEFAULT_ZOOM
import com.joesemper.fishing.domain.viewstates.BaseViewState
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt


const val MILLISECONDS_IN_DAY = 86400000L
const val SECONDS_IN_DAY = 86400L
const val MILLISECONDS_IN_SECOND = 1000L
const val MILLISECONDS_IN_HOUR = 3600000L
const val SECONDS_IN_HOUR = 3600L
const val SECONDS_IN_MINUTE = 60L
const val MOON_PHASE_INCREMENT_IN_DAY = 0.03f

fun getNewMarkerNoteId() = getRandomString(6)
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

fun String.onlyLetters() = all { it.isLetter() }

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun Float.roundTo(numFractionDigits: Int): Float {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor.toFloat()
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

fun hPaToMmHg(pressure: Int): Int {
    return (pressure * 0.75006375541921).toInt()
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun getCameraPosition(latLng: LatLng): Pair<LatLng, Float> {
    val lat = latLng.latitude + ((-100..100).random() * 0.000000001)
    val lng = latLng.longitude + ((-100..100).random() * 0.000000001)
    return Pair(LatLng(lat, lng), DEFAULT_ZOOM)
}

