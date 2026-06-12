package com.mobileprism.fishing.ui.utils.format

import kotlin.test.Test
import kotlin.test.assertEquals

class MeasurementFormatterTest {

    @Test
    fun weightDropsTrailingZeroForWholeNumber() {
        assertEquals("3", MeasurementFormatter.weight(3.0))
    }

    @Test
    fun weightKeepsTwoDecimalsAndTrimsTrailingZero() {
        assertEquals("1.5", MeasurementFormatter.weight(1.50))
        assertEquals("1.25", MeasurementFormatter.weight(1.25))
    }

    @Test
    fun weightRoundsToTwoDecimals() {
        assertEquals("1.24", MeasurementFormatter.weight(1.23999))
        assertEquals("0.07", MeasurementFormatter.weight(0.07))
    }

    @Test
    fun weightHandlesZero() {
        assertEquals("0", MeasurementFormatter.weight(0.0))
    }

    @Test
    fun amountRendersInteger() {
        assertEquals("42", MeasurementFormatter.amount(42))
    }

    @Test
    fun decimalRespectsMaxDecimals() {
        assertEquals("3.142", MeasurementFormatter.decimal(3.14159, maxDecimals = 3))
    }
}
