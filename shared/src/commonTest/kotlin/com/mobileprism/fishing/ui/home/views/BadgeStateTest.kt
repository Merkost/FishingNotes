package com.mobileprism.fishing.ui.home.views

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BadgeStateTest {

    @Test
    fun countBadgeHiddenForZeroAndNegative() {
        assertFalse(shouldShowCountBadge(0))
        assertFalse(shouldShowCountBadge(-1))
    }

    @Test
    fun countBadgeShownForPositive() {
        assertTrue(shouldShowCountBadge(1))
        assertTrue(shouldShowCountBadge(150))
    }

    @Test
    fun statusVariantsAreDistinct() {
        assertEquals(4, StatusLabelVariant.entries.size)
    }

    @Test
    fun emptyStatValueIsEmDash() {
        assertEquals("—", EmptyStatValue)
    }
}
