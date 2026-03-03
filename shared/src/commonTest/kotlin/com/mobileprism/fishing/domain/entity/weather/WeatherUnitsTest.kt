package com.mobileprism.fishing.domain.entity.weather

import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherUnitsTest {

    // ---- TemperatureValues ----

    @Test
    fun temperatureCelsiusPassthrough() {
        assertEquals("20", TemperatureValues.C.getTemperature(20.0f))
    }

    @Test
    fun temperatureFahrenheitConversion() {
        // 20 * 9/5 + 32 = 68
        assertEquals("68", TemperatureValues.F.getTemperature(20.0f))
    }

    @Test
    fun temperatureKelvinConversion() {
        // (20 + 273.15).toInt() = 293
        assertEquals("293", TemperatureValues.K.getTemperature(20.0f))
    }

    // ---- PressureValues ----

    @Test
    fun pressureHpaPassthrough() {
        assertEquals("1013", PressureValues.Hpa.getPressureFromHpa(1013))
    }

    @Test
    fun pressureMmHgConversion() {
        // (1013 * 0.75006375541921).toInt() = 759
        assertEquals("759", PressureValues.mmHg.getPressureFromHpa(1013))
    }

    @Test
    fun pressurePaConversion() {
        // 1013 * 100 = 101300
        assertEquals("101300", PressureValues.Pa.getPressureFromHpa(1013))
    }

    // ---- WindSpeedValues ----

    @Test
    fun windSpeedMetersPerSecondPassthrough() {
        // ((5.0 * 10).toInt() / 10.0) = 5.0
        assertEquals("5.0", WindSpeedValues.metersps.getWindSpeed(5.0))
    }

    @Test
    fun windSpeedKmphConversion() {
        // 5.0 * 3.6 = 18.0; ((18.0 * 10).toInt() / 10.0) = 18.0
        assertEquals("18.0", WindSpeedValues.kmph.getWindSpeed(5.0))
    }

    @Test
    fun windSpeedKnotsConversion() {
        // 5.0 * 1.9438444924574 = 9.719222...
        // (9.719222 * 10).toInt() = 97; 97 / 10.0 = 9.7
        assertEquals("9.7", WindSpeedValues.knots.getWindSpeed(5.0))
    }
}
