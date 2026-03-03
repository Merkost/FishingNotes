package com.mobileprism.fishing.ui.home.map

import kotlin.math.roundToInt

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
