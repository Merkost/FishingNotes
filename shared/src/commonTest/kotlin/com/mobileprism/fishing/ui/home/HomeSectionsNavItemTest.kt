package com.mobileprism.fishing.ui.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeSectionsNavItemTest {

    @Test
    fun homeNavItems_hasOneItemPerSection_inOrder() {
        val items = homeNavItems()
        assertEquals(HomeSections.entries.size, items.size)
        assertEquals(
            HomeSections.entries.map { it.name },
            items.map { it.first },
        )
    }

    @Test
    fun homeNavItems_iconsMatchSectionIcons() {
        val items = homeNavItems()
        HomeSections.entries.forEachIndexed { index, section ->
            assertEquals(section.icon, items[index].second)
        }
    }

    @Test
    fun homeNavItems_titleResourcesMatchSectionTitles() {
        val items = homeNavItems()
        HomeSections.entries.forEachIndexed { index, section ->
            assertEquals(section.title, items[index].third)
        }
    }
}
