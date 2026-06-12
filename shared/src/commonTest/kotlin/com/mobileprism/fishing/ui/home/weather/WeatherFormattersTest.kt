package com.mobileprism.fishing.ui.home.weather

import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherFormattersTest {

    @Test
    fun probabilityToPercent_rounds_down_to_int() {
        assertEquals(0, probabilityToPercent(0.0f))
        assertEquals(20, probabilityToPercent(0.2f))
        assertEquals(99, probabilityToPercent(0.999f))
        assertEquals(100, probabilityToPercent(1.0f))
    }

    @Test
    fun moonPhaseToPercent_rounds_down_to_int() {
        assertEquals(0, moonPhaseToPercent(0.0f))
        assertEquals(50, moonPhaseToPercent(0.5f))
        assertEquals(100, moonPhaseToPercent(1.0f))
    }

    @Test
    fun isHeavyPrecipitation_threshold_is_20_percent() {
        assertEquals(false, isHeavyPrecipitation(0.19f))
        assertEquals(true, isHeavyPrecipitation(0.2f))
        assertEquals(true, isHeavyPrecipitation(0.5f))
    }
}
