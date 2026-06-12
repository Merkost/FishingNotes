package com.mobileprism.fishing.ui.components

import androidx.compose.material3.Typography
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.home.views.textStyleFor
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTextStyleTest {

    private val typo = Typography()

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

    @Test
    fun eachStyleMapsToItsM3Role() {
        assertEquals(typo.displaySmall, textStyleFor(AppTextStyle.Display, typo))
        assertEquals(typo.headlineSmall, textStyleFor(AppTextStyle.Heading, typo))
        assertEquals(typo.titleLarge, textStyleFor(AppTextStyle.Title, typo))
        assertEquals(typo.titleMedium, textStyleFor(AppTextStyle.Subtitle, typo))
        assertEquals(typo.bodyLarge, textStyleFor(AppTextStyle.Body, typo))
        assertEquals(typo.bodyMedium, textStyleFor(AppTextStyle.BodySmall, typo))
        assertEquals(typo.bodySmall, textStyleFor(AppTextStyle.Caption, typo))
        assertEquals(typo.labelMedium, textStyleFor(AppTextStyle.Support, typo))
    }
}
