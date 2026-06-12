package com.mobileprism.fishing.utils

import com.mobileprism.fishing.domain.entity.weather.Hourly
import com.mobileprism.fishing.testutils.userMapMarker
import com.mobileprism.fishing.utils.time.TimeConstants.MOON_ZERO_DATE_SECONDS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationUtilsNoteTest {

    @Test
    fun blankNoteIsInvalid() {
        assertFalse(ValidationUtils.isNoteValid(""))
        assertFalse(ValidationUtils.isNoteValid("   "))
        assertFalse(ValidationUtils.isNoteValid("\t\n"))
    }

    @Test
    fun nonBlankNoteIsValid() {
        assertTrue(ValidationUtils.isNoteValid("Hello"))
        assertTrue(ValidationUtils.isNoteValid(" a "))
    }
}

class UtilFunctionsTest {

    // ---- calcMoonPhase ----

    @Test
    fun calcMoonPhaseReturnsValueInZeroToOneRange() {
        // An arbitrary date well after the zero date
        val date = MOON_ZERO_DATE_SECONDS + 86400L * 20
        val phase = calcMoonPhase(date)
        assertTrue(phase in 0f..1f, "Moon phase should be in [0,1] but was $phase")
    }

    @Test
    fun calcMoonPhaseAtZeroDateReturnsApproximatelyZero() {
        val phase = calcMoonPhase(MOON_ZERO_DATE_SECONDS)
        assertTrue(phase < 0.01f, "Moon phase at zero date should be ~0 but was $phase")
    }

    // ---- isDateInList ----

    @Test
    fun isDateInListReturnsTrueWhenDateMatchesAnHourlyEntry() {
        // 1700000000 seconds -> some hour H; Hourly with the same date should match
        val baseDate = 1700000000L
        val hourlyList = listOf(
            Hourly(date = baseDate),
            Hourly(date = baseDate + 3600L),
        )
        assertTrue(isDateInList(hourlyList, baseDate))
    }

    @Test
    fun isDateInListReturnsFalseWhenNoMatch() {
        val baseDate = 1700000000L
        val hourlyList = listOf(
            Hourly(date = baseDate),
        )
        // A date many hours away should not match
        val differentDate = baseDate + 3600L * 100
        assertFalse(isDateInList(hourlyList, differentDate))
    }

    // ---- getClosestHourIndex ----

    @Test
    fun getClosestHourIndexReturnsCorrectIndexForMatchingDate() {
        val baseDate = 1700000000L
        val hourlyList = listOf(
            Hourly(date = baseDate),
            Hourly(date = baseDate + 3600L),
            Hourly(date = baseDate + 7200L),
        )
        // The second element (index 1) matches baseDate + 3600
        val index = getClosestHourIndex(hourlyList, baseDate + 3600L)
        assertEquals(1, index)
    }

    @Test
    fun getClosestHourIndexReturnsZeroWhenNoMatch() {
        val baseDate = 1700000000L
        val hourlyList = listOf(
            Hourly(date = baseDate),
        )
        val index = getClosestHourIndex(hourlyList, baseDate + 3600L * 200)
        assertEquals(0, index)
    }

    // ---- isLocationsTooFar ----

    @Test
    fun isLocationsTooFarReturnsTrueForDistantLocations() {
        val first = userMapMarker(latitude = 55.0, longitude = 37.0)
        val second = userMapMarker(latitude = 56.0, longitude = 37.0)
        assertTrue(isLocationsTooFar(first, second))
    }

    @Test
    fun isLocationsTooFarReturnsFalseForCloseLocations() {
        val first = userMapMarker(latitude = 55.0, longitude = 37.0)
        val second = userMapMarker(latitude = 55.01, longitude = 37.0)
        assertFalse(isLocationsTooFar(first, second))
    }

    // ---- getRandomString ----

    @Test
    fun getRandomStringReturnsStringOfCorrectLength() {
        assertEquals(10, getRandomString(10).length)
        assertEquals(1, getRandomString(1).length)
        assertEquals(50, getRandomString(50).length)
    }

    @Test
    fun getRandomStringContainsOnlyAlphanumericChars() {
        val result = getRandomString(100)
        assertTrue(result.all { it.isLetterOrDigit() }, "Expected alphanumeric chars only: $result")
    }
}
