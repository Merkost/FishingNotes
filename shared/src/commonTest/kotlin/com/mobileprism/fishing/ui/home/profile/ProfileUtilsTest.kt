package com.mobileprism.fishing.ui.home.profile

import com.mobileprism.fishing.testutils.userCatch
import com.mobileprism.fishing.testutils.userMapMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileUtilsTest {

    // ---- findBestCatch ----

    @Test
    fun findBestCatchReturnsCatchWithMaxFishWeight() {
        val catches = listOf(
            userCatch(id = "c1", fishWeight = 1.5),
            userCatch(id = "c2", fishWeight = 4.2),
            userCatch(id = "c3", fishWeight = 3.0),
        )
        val best = findBestCatch(catches)
        assertEquals("c2", best?.id)
        assertEquals(4.2, best?.fishWeight)
    }

    @Test
    fun findBestCatchReturnsNullForEmptyList() {
        assertNull(findBestCatch(emptyList()))
    }

    // ---- findFavoritePlace ----

    @Test
    fun findFavoritePlaceReturnsPlaceWithMaxCatchesCount() {
        val places = listOf(
            userMapMarker(id = "m1", catchesCount = 3),
            userMapMarker(id = "m2", catchesCount = 10),
            userMapMarker(id = "m3", catchesCount = 7),
        )
        val favorite = findFavoritePlace(places)
        assertEquals("m2", favorite?.id)
        assertEquals(10, favorite?.catchesCount)
    }

    @Test
    fun findFavoritePlaceReturnsNullForEmptyList() {
        assertNull(findFavoritePlace(emptyList()))
    }
}
