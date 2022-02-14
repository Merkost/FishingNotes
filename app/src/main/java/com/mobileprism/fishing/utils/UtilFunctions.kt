package com.mobileprism.fishing.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.compose.ui.home.map.DEFAULT_ZOOM
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.utils.time.TimeConstants.MOON_PHASE_INCREMENT_IN_DAY
import com.mobileprism.fishing.utils.time.TimeConstants.MOON_ZERO_DATE_SECONDS
import com.mobileprism.fishing.utils.time.TimeConstants.SECONDS_IN_DAY
import com.mobileprism.fishing.utils.time.formatToMilliseconds
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


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

//fun calcMoonPhase(currentPhase: Float, currentDate: Long, requiredDate: Long): Float {
//    var result = currentPhase
//    val dif = currentDate - requiredDate
//    val numOfDays = dif / SECONDS_IN_DAY
//    result -= (numOfDays * MOON_PHASE_INCREMENT_IN_DAY)
//    if (result < 0.0f) {
//        result += 1.0f
//    }
//    return result
//}

fun calcMoonPhase(requiredDate: Long): Float {
    val dif = formatToMilliseconds(requiredDate) - formatToMilliseconds(MOON_ZERO_DATE_SECONDS)
    val numOfDays = dif / SECONDS_IN_DAY
    val phase = numOfDays * MOON_PHASE_INCREMENT_IN_DAY
    val numOfFullCycles = phase.toInt()
    return (phase - numOfFullCycles)
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun getCameraPosition(latLng: LatLng): Pair<LatLng, Float> {
    val lat = latLng.latitude + ((-100..100).random() * 0.000000001)
    val lng = latLng.longitude + ((-100..100).random() * 0.000000001)
    return Pair(LatLng(lat, lng), DEFAULT_ZOOM)
}

fun isLocationsTooFar(first: UserMapMarker, second: UserMapMarker): Boolean {
    return (sqrt(
        ((first.latitude - second.latitude).pow(2))
                + ((first.longitude - second.longitude).pow(2))
    ) > 0.3)
}

fun isCoordinatesFar(first: LatLng, second: LatLng): Boolean {
    return (sqrt(
        ((first.latitude - second.latitude).pow(2))
                + ((first.longitude - second.longitude).pow(2))
    ) > 0.1)
}

