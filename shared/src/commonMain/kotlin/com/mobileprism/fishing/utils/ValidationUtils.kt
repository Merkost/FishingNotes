package com.mobileprism.fishing.utils

import kotlin.time.Clock

object ValidationUtils {
    const val MAX_FISH_WEIGHT_KG = 500.0
    const val MIN_FISH_WEIGHT_KG = 0.0
    const val MAX_FISH_AMOUNT = 9999
    const val MIN_FISH_AMOUNT = 0
    const val MAX_PLACE_NAME_LENGTH = 50
    const val MAX_NOTE_TITLE_LENGTH = 100
    const val MIN_LATITUDE = -90.0
    const val MAX_LATITUDE = 90.0
    const val MIN_LONGITUDE = -180.0
    const val MAX_LONGITUDE = 180.0

    fun isWeightValid(weight: Double): Boolean =
        weight in MIN_FISH_WEIGHT_KG..MAX_FISH_WEIGHT_KG

    fun isAmountValid(amount: Int): Boolean =
        amount in MIN_FISH_AMOUNT..MAX_FISH_AMOUNT

    fun isCoordinateValid(latitude: Double, longitude: Double): Boolean =
        latitude in MIN_LATITUDE..MAX_LATITUDE && longitude in MIN_LONGITUDE..MAX_LONGITUDE

    fun isNoteValid(description: String): Boolean = description.isNotBlank()

    fun isValidNoteTitle(title: String): Boolean =
        title.isBlank() || title.trim().length <= MAX_NOTE_TITLE_LENGTH

    fun clampDate(date: Long): Long =
        minOf(date, Clock.System.now().toEpochMilliseconds())
}
