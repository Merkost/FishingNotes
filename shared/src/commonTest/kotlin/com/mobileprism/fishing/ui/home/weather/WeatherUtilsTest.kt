package com.mobileprism.fishing.ui.home.weather

import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherUtilsTest {

    private val forecast = listOf(
        Daily(pressure = 1013),
        Daily(pressure = 1000),
    )

    @Test
    fun getPressureList_hpa_returns_raw_hpa() {
        assertEquals(listOf(1013, 1000), getPressureList(forecast, PressureValues.Hpa))
    }

    @Test
    fun getPressureList_mmhg_converts_each_value() {
        val expected = forecast.map { PressureValues.mmHg.getPressureFromHpa(it.pressure).toInt() }
        assertEquals(expected, getPressureList(forecast, PressureValues.mmHg))
    }
}
