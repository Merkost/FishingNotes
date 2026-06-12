package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppButtonStyleTest {

    @Test
    fun appButtonStyleHasFourVariants() {
        assertEquals(4, AppButtonStyle.entries.size)
    }

    @Test
    fun appButtonStyleEntriesInDocumentedOrder() {
        assertEquals(
            listOf("Filled", "Tonal", "Outlined", "Text"),
            AppButtonStyle.entries.map { it.name },
        )
    }
}
