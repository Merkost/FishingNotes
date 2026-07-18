package com.mobileprism.fishing.domain.use_cases.auth

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlin.test.Test
import kotlin.test.assertEquals

class GuestMergePlanTest {
    private fun marker(id: String) = UserMapMarker(id = id)
    private fun catch(id: String) = UserCatch(id = id)

    @Test
    fun `copies only ids not already present`() {
        val plan = planGuestMerge(
            guestMarkers = listOf(marker("m1"), marker("m2")),
            existingMarkers = listOf(marker("m2")),
            guestCatches = listOf(catch("c1")),
            existingCatches = emptyList(),
        )
        assertEquals(listOf("m1"), plan.markersToCopy.map { it.id })
        assertEquals(listOf("c1"), plan.catchesToCopy.map { it.id })
        assertEquals(1, plan.alreadyPresent)
    }

    @Test
    fun `idempotent when everything already present`() {
        val plan = planGuestMerge(
            guestMarkers = listOf(marker("m1")),
            existingMarkers = listOf(marker("m1")),
            guestCatches = listOf(catch("c1")),
            existingCatches = listOf(catch("c1")),
        )
        assertEquals(emptyList(), plan.markersToCopy)
        assertEquals(emptyList(), plan.catchesToCopy)
        assertEquals(2, plan.alreadyPresent)
    }
}
