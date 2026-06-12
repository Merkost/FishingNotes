package com.mobileprism.fishing.ui.utils.format

import kotlin.math.roundToLong

object MeasurementFormatter {

    fun weight(value: Double): String = trimNumber(roundTo(value, 2))

    fun amount(value: Int): String = value.toString()

    fun decimal(value: Double, maxDecimals: Int = 2): String = trimNumber(roundTo(value, maxDecimals))

    private fun roundTo(value: Double, decimals: Int): Double {
        var factor = 1.0
        repeat(decimals) { factor *= 10.0 }
        return (value * factor).roundToLong() / factor
    }

    private fun trimNumber(value: Double): String {
        if (value == value.toLong().toDouble()) return value.toLong().toString()
        return value.toString().trimEnd('0').trimEnd('.')
    }
}
