package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.home.views.AppTextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTextStyleTest {

    @Test
    fun appTextStyleHasEightRoles() {
        assertEquals(8, AppTextStyle.entries.size)
    }

    @Test
    fun appTextStyleEntriesInDocumentedOrder() {
        assertEquals(
            listOf(
                "Display", "Heading", "Title", "Subtitle",
                "Body", "BodySmall", "Caption", "Support",
            ),
            AppTextStyle.entries.map { it.name },
        )
    }
}
