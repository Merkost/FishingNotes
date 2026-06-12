package com.mobileprism.fishing.ui.utils

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.month_apr
import fishing.shared.generated.resources.month_dec
import fishing.shared.generated.resources.month_jan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MonthLabelsTest {

    @Test
    fun maps_first_month_to_jan_resource() {
        assertEquals(Res.string.month_jan, monthShortLabelResource("2025-01"))
    }

    @Test
    fun maps_fourth_month_to_apr_resource() {
        assertEquals(Res.string.month_apr, monthShortLabelResource("2024-04"))
    }

    @Test
    fun maps_twelfth_month_to_dec_resource() {
        assertEquals(Res.string.month_dec, monthShortLabelResource("2025-12"))
    }

    @Test
    fun returns_null_for_month_out_of_range() {
        assertNull(monthShortLabelResource("2025-13"))
        assertNull(monthShortLabelResource("2025-00"))
    }

    @Test
    fun returns_null_for_malformed_key() {
        assertNull(monthShortLabelResource("2025"))
        assertNull(monthShortLabelResource("not-a-date"))
        assertNull(monthShortLabelResource(""))
    }
}
