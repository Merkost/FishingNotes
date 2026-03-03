package com.mobileprism.fishing.model.mappers

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherParsersTest {

    @Test
    fun getMoonIconByPhase_newMoon_atZero() {
        assertEquals(Res.drawable.moon_new, getMoonIconByPhase(0.0f))
    }

    @Test
    fun getMoonIconByPhase_newMoon_atBoundary() {
        assertEquals(Res.drawable.moon_new, getMoonIconByPhase(0.02f))
    }

    @Test
    fun getMoonIconByPhase_waxingCrescent() {
        assertEquals(Res.drawable.moon_waxing_crescent, getMoonIconByPhase(0.1f))
    }

    @Test
    fun getMoonIconByPhase_fullMoon() {
        assertEquals(Res.drawable.moon_full, getMoonIconByPhase(0.55f))
    }

    @Test
    fun getMoonIconByPhase_waningCrescent() {
        assertEquals(Res.drawable.moon_waning_crescent, getMoonIconByPhase(0.98f))
    }

    @Test
    fun getMoonIconByPhase_nearOne_returnsNewMoon() {
        assertEquals(Res.drawable.moon_new, getMoonIconByPhase(0.99f))
    }

    @Test
    fun getMoonIconByPhase_exactlyOne_returnsNewMoon() {
        assertEquals(Res.drawable.moon_new, getMoonIconByPhase(1.0f))
    }

    @Test
    fun getMoonIconByPhase_firstQuarter() {
        assertEquals(Res.drawable.moon_first_quarter, getMoonIconByPhase(0.25f))
    }

    @Test
    fun getMoonIconByPhase_waxingGibbous() {
        assertEquals(Res.drawable.moon_waxing_gibbous, getMoonIconByPhase(0.35f))
    }

    @Test
    fun getMoonIconByPhase_waningGibbous() {
        assertEquals(Res.drawable.moon_waning_gibbous, getMoonIconByPhase(0.65f))
    }

    @Test
    fun getMoonIconByPhase_lastQuarter() {
        assertEquals(Res.drawable.moon_last_quarter, getMoonIconByPhase(0.87f))
    }
}
