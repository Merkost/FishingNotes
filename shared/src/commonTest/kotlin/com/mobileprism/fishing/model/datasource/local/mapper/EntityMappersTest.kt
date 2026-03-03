package com.mobileprism.fishing.model.datasource.local.mapper

import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import com.mobileprism.fishing.testutils.catchEntity
import com.mobileprism.fishing.testutils.markerEntity
import com.mobileprism.fishing.testutils.note
import com.mobileprism.fishing.testutils.userCatch
import com.mobileprism.fishing.testutils.userMapMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityMappersTest {

    // ---- UserCatch <-> CatchEntity ----

    @Test
    fun userCatchRoundTripPreservesAllFields() {
        val photos = listOf("http://example.com/a.jpg", "http://example.com/b.jpg")
        val catchNote = note(id = "n1", title = "Big fish", description = "Caught at dawn", dateCreated = 170000L)
        val original = userCatch(
            id = "c-42",
            userId = "u-7",
            date = 1710000000000L,
            fishType = "Trout",
            fishWeight = 3.7,
            fishAmount = 2,
            userMarkerId = "m-99",
            placeTitle = "River Spot",
            downloadPhotoLinks = photos,
            weatherPrimary = "Cloudy",
            weatherTemperature = 15.5f,
            weatherMoonPhase = 0.25f,
            weatherWindSpeed = 5.0f,
            weatherWindDeg = 90,
            weatherPressure = 1020,
            note = catchNote,
            fishingRodType = "Spinning",
            fishingBait = "Worm",
            fishingLure = "Spoon",
            lastModified = 1710000000000L,
        )

        val restored = original.toEntity().toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.description, restored.description)
        assertEquals(original.note, restored.note)
        assertEquals(original.date, restored.date)
        assertEquals(original.fishType, restored.fishType)
        assertEquals(original.fishAmount, restored.fishAmount)
        assertEquals(original.fishWeight, restored.fishWeight, 0.001)
        assertEquals(original.fishingRodType, restored.fishingRodType)
        assertEquals(original.fishingBait, restored.fishingBait)
        assertEquals(original.fishingLure, restored.fishingLure)
        assertEquals(original.userMarkerId, restored.userMarkerId)
        assertEquals(original.placeTitle, restored.placeTitle)
        assertEquals(original.isPublic, restored.isPublic)
        assertEquals(original.downloadPhotoLinks, restored.downloadPhotoLinks)
        assertEquals(original.weatherPrimary, restored.weatherPrimary)
        assertEquals(original.weatherTemperature, restored.weatherTemperature, 0.001f)
        assertEquals(original.weatherWindSpeed, restored.weatherWindSpeed, 0.001f)
        assertEquals(original.weatherWindDeg, restored.weatherWindDeg)
        assertEquals(original.weatherPressure, restored.weatherPressure)
        assertEquals(original.weatherMoonPhase, restored.weatherMoonPhase, 0.001f)
        assertEquals(original.lastModified, restored.lastModified)
    }

    @Test
    fun catchEntityWithValidPhotoLinksDeserializesCorrectly() {
        val entity = catchEntity(
            downloadPhotoLinks = """["http://example.com/photo1.jpg","http://example.com/photo2.jpg"]"""
        )

        val domain = entity.toDomain()

        assertEquals(2, domain.downloadPhotoLinks.size)
        assertEquals("http://example.com/photo1.jpg", domain.downloadPhotoLinks[0])
        assertEquals("http://example.com/photo2.jpg", domain.downloadPhotoLinks[1])
    }

    @Test
    fun catchEntityWithCorruptPhotoLinksReturnsEmptyList() {
        val entity = catchEntity(downloadPhotoLinks = "not-valid-json{{{")

        val domain = entity.toDomain()

        assertTrue(domain.downloadPhotoLinks.isEmpty())
    }

    @Test
    fun toEntitySerializesDownloadPhotoLinksToJson() {
        val photos = listOf("http://a.com/1.jpg", "http://b.com/2.jpg")
        val catch = userCatch(downloadPhotoLinks = photos)

        val entity = catch.toEntity()

        // The serialized string should be valid JSON containing both URLs
        assertTrue(entity.downloadPhotoLinks.contains("http://a.com/1.jpg"))
        assertTrue(entity.downloadPhotoLinks.contains("http://b.com/2.jpg"))
        assertTrue(entity.downloadPhotoLinks.startsWith("["))
        assertTrue(entity.downloadPhotoLinks.endsWith("]"))
    }

    @Test
    fun syncStatusParameterAppliedCorrectlyToCatchEntity() {
        val catch = userCatch()

        val defaultEntity = catch.toEntity()
        assertEquals(SyncStatus.SYNCED, defaultEntity.syncStatus)

        val pendingEntity = catch.toEntity(syncStatus = SyncStatus.PENDING_CREATE)
        assertEquals(SyncStatus.PENDING_CREATE, pendingEntity.syncStatus)

        val updateEntity = catch.toEntity(syncStatus = SyncStatus.PENDING_UPDATE)
        assertEquals(SyncStatus.PENDING_UPDATE, updateEntity.syncStatus)
    }

    // ---- UserMapMarker <-> MarkerEntity ----

    @Test
    fun userMapMarkerRoundTripPreservesAllFields() {
        val notes = listOf(
            note(id = "n1", title = "First", description = "Desc1", dateCreated = 100L),
            note(id = "n2", title = "Second", description = "Desc2", dateCreated = 200L),
        )
        val original = userMapMarker(
            id = "m-10",
            userId = "u-3",
            latitude = 48.8566,
            longitude = 2.3522,
            title = "Paris Spot",
            catchesCount = 5,
            notes = notes,
            description = "Near the bridge",
            markerColor = 0x00FF00,
            dateOfCreation = 1700000000000L,
            visible = false,
            public = true,
            lastModified = 1700000000000L,
        )

        val restored = original.toEntity().toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.latitude, restored.latitude, 0.0001)
        assertEquals(original.longitude, restored.longitude, 0.0001)
        assertEquals(original.title, restored.title)
        assertEquals(original.description, restored.description)
        assertEquals(original.markerColor, restored.markerColor)
        assertEquals(original.catchesCount, restored.catchesCount)
        assertEquals(original.dateOfCreation, restored.dateOfCreation)
        assertEquals(original.visible, restored.visible)
        assertEquals(original.public, restored.public)
        assertEquals(original.notes, restored.notes)
        assertEquals(original.lastModified, restored.lastModified)
    }

    @Test
    fun markerEntityWithValidNotesJsonDeserializesCorrectly() {
        val entity = markerEntity(
            notes = """[{"id":"n1","title":"Note A","description":"Desc A","dateCreated":100},{"id":"n2","title":"Note B","description":"Desc B","dateCreated":200}]"""
        )

        val domain = entity.toDomain()

        assertEquals(2, domain.notes.size)
        assertEquals("n1", domain.notes[0].id)
        assertEquals("Note A", domain.notes[0].title)
        assertEquals("Desc A", domain.notes[0].description)
        assertEquals(100L, domain.notes[0].dateCreated)
        assertEquals("n2", domain.notes[1].id)
        assertEquals("Note B", domain.notes[1].title)
    }

    @Test
    fun markerEntityWithCorruptNotesJsonReturnsEmptyList() {
        val entity = markerEntity(notes = "corrupt{json[")

        val domain = entity.toDomain()

        assertTrue(domain.notes.isEmpty())
    }

    @Test
    fun toEntityForMarkerSerializesNotesToJson() {
        val notes = listOf(
            note(id = "n1", title = "Note", description = "Desc", dateCreated = 500L)
        )
        val marker = userMapMarker(notes = notes)

        val entity = marker.toEntity()

        assertTrue(entity.notes.contains("n1"))
        assertTrue(entity.notes.contains("Note"))
        assertTrue(entity.notes.startsWith("["))
        assertTrue(entity.notes.endsWith("]"))
    }

    @Test
    fun markerColorPreservedThroughRoundTrip() {
        val customColor = 0x123456
        val marker = userMapMarker(markerColor = customColor)

        val restored = marker.toEntity().toDomain()

        assertEquals(customColor, restored.markerColor)
    }
}
