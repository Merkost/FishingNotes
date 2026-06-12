package com.mobileprism.fishing.ui.utils.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateLabelsTest {

    @Test
    fun parsesValidMonthKey() {
        assertEquals(1, DateLabels.monthNumberFromKey("2025-01"))
        assertEquals(12, DateLabels.monthNumberFromKey("2024-12"))
    }

    @Test
    fun returnsNullForMalformedKey() {
        assertNull(DateLabels.monthNumberFromKey("2025"))
        assertNull(DateLabels.monthNumberFromKey("2025-13"))
        assertNull(DateLabels.monthNumberFromKey("2025-00"))
        assertNull(DateLabels.monthNumberFromKey("abc-de"))
    }
}
