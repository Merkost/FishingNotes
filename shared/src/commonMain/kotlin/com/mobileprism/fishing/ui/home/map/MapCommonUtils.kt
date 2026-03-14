package com.mobileprism.fishing.ui.home.map

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun getHue(red: Float, green: Float, blue: Float): Float {
    val min = min(min(red, green), blue)
    val max = max(max(red, green), blue)
    val c = max - min
    if (min == max) {
        return 0f
    }
    var hue = 0f
    when (max) {
        red -> {
            val segment = (green - blue) / c
            var shift = 0 / 60
            if (segment < 0) {
                shift = 360 / 60
            }
            hue = segment + shift
        }
        green -> {
            val segment = (blue - red) / c
            val shift = 120 / 60
            hue = segment + shift
        }
        blue -> {
            val segment = (red - green) / c
            val shift = 240 / 60
            hue = segment + shift
        }
    }
    return hue * 60
}

fun convertDistance(distanceInMeters: Double, mLabel: String, kmLabel: String): String {
    return when (distanceInMeters.toInt()) {
        in 0..999 -> "${distanceInMeters.toInt()} $mLabel"
        in 1000..9999 -> {
            val km = distanceInMeters / 1000.0
            val rounded = (km * 10).roundToInt() / 10.0
            "$rounded $kmLabel"
        }
        else -> "${distanceInMeters.div(1000).toInt()} $kmLabel"
    }
}
