package com.mobileprism.fishing.ui.home.new_catch

import kotlin.test.Test
import kotlin.test.assertEquals

class CatchSummaryFormatterTest {

    private fun format(
        fishName: String,
        amount: Int,
        weight: Double,
    ): String = CatchSummaryFormatter.format(
        fishName = fishName,
        amount = amount,
        weight = weight,
        kgSuffix = "kg",
        countTemplate = "×%1\$d",
        separator = " · ",
    )

    @Test
    fun nameOnlyWhenAmountAndWeightAreZero() {
        assertEquals("Pike", format("Pike", amount = 0, weight = 0.0))
    }

    @Test
    fun appendsCountWhenAmountPositive() {
        assertEquals("Pike ×3", format("Pike", amount = 3, weight = 0.0))
    }

    @Test
    fun appendsWeightWithoutTrailingZeroes() {
        assertEquals("Pike · 0.5kg", format("Pike", amount = 0, weight = 0.5))
    }

    @Test
    fun wholeWeightHasNoDecimalPart() {
        assertEquals("Pike · 2kg", format("Pike", amount = 0, weight = 2.0))
    }

    @Test
    fun appendsBothCountAndWeightInOrder() {
        assertEquals("Pike ×2 · 1.5kg", format("Pike", amount = 2, weight = 1.5))
    }

    @Test
    fun blankNameWithNoMetricsIsEmpty() {
        assertEquals("", format("", amount = 0, weight = 0.0))
    }
}
