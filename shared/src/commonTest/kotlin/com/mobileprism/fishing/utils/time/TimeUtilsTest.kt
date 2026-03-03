package com.mobileprism.fishing.utils.time

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUtilsTest {

    // ---- hoursCount ----

    @Test
    fun hoursCountForMillisecondValueReturnsCorrectHours() {
        // 7200000 ms = 2 hours; value > 1_000_000_000_000 is false so it gets *1000,
        // but 7200000 < 1_000_000_000_000, so formatToMilliseconds returns 7200000 * 1000 = 7_200_000_000
        // 7_200_000_000 / 3_600_000 = 2000
        // To get exactly 2 hours from a millisecond timestamp, we need a value > 1_000_000_000_000
        // so it passes through as-is: e.g. 7_200_000L ms but we need it > 1T.
        // Actually, let's use a real millisecond timestamp: 1_700_007_200_000 (> 1T, passes through)
        // 1_700_007_200_000 / 3_600_000 = 472224.222... -> 472224
        // Simpler: 3_600_000L * 2 = 7_200_000L -- but that's < 1T, so it becomes 7_200_000_000L
        // 7_200_000_000 / 3_600_000 = 2000. So for a raw millis value < 1T we get a huge number.

        // Use a value > 1_000_000_000_000 to ensure it passes through as milliseconds
        val twoHoursMs = TimeConstants.MILLISECONDS_IN_HOUR * 2 // 7_200_000
        // This is < 1T so formatToMilliseconds multiplies by 1000 -> 7_200_000_000
        // hoursCount = 7_200_000_000 / 3_600_000 = 2000
        assertEquals(2000, twoHoursMs.hoursCount())

        // For a true millisecond timestamp (> 1T), it passes through:
        val realMillisTimestamp = 1_700_000_000_000L + TimeConstants.MILLISECONDS_IN_HOUR * 2
        val baseHours = (1_700_000_000_000L / TimeConstants.MILLISECONDS_IN_HOUR).toInt()
        assertEquals(baseHours + 2, realMillisTimestamp.hoursCount())
    }

    @Test
    fun hoursCountForSecondValueConvertsCorrectly() {
        // 7200 seconds; < 1_000_000_000_000, so formatToMilliseconds returns 7200 * 1000 = 7_200_000
        // 7_200_000 / 3_600_000 = 2
        val twoHoursInSeconds = 7200L
        assertEquals(2, twoHoursInSeconds.hoursCount())
    }

    // ---- daysCount ----

    @Test
    fun daysCountConversionIsCorrect() {
        // 172800 seconds = 2 days; < 1T so *1000 = 172_800_000
        // 172_800_000 / 86_400_000 = 2
        val twoDaysInSeconds = 172800L
        assertEquals(2, twoDaysInSeconds.daysCount())
    }

    // ---- formatToMilliseconds ----

    @Test
    fun formatToMillisecondsPassesThroughLargeValues() {
        val alreadyMillis = 1_700_000_000_000L
        assertEquals(alreadyMillis, formatToMilliseconds(alreadyMillis))
    }

    @Test
    fun formatToMillisecondsMultipliesSmallerValues() {
        val seconds = 1_700_000_000L
        assertEquals(1_700_000_000_000L, formatToMilliseconds(seconds))
    }
}
