package com.mobileprism.fishing.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.catch.CatchTitleView
import com.mobileprism.fishing.ui.home.catch.WayOfFishingView
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.views.DefaultNoteView

private val sampleCatch = UserCatch(
    id = "sample",
    userId = "demo",
    note = Note(
        id = "note-1",
        title = "Morning session",
        description = "Slow-rolled a gold lure across the drop-off. Hit on the third cast, clean release at 5:50 AM.",
        dateCreated = 1780688520000L,
    ),
    date = 1780688520000L,
    fishType = "Barramundi",
    fishAmount = 1,
    fishWeight = 4.8,
    fishingRodType = "Shimano Sustain 4000",
    fishingBait = "Live mullet",
    fishingLure = "Gold Bomber 15A",
    placeTitle = "Lake Tinaroo",
    weatherPrimary = "Partly cloudy",
    weatherIcon = "02d",
    weatherTemperature = 22f,
    weatherWindSpeed = 8f,
    weatherWindDeg = 45,
    weatherPressure = 762,
    weatherMoonPhase = 0.65f,
)

private val samplePlace = UserMapMarker(
    id = "place-1",
    title = "Lake Tinaroo",
    description = "Favorite barra spot",
    latitude = -17.1708,
    longitude = 145.5958,
    catchesCount = 12,
    markerColor = 0xFFE53935.toInt(),
    visible = true,
    dateOfCreation = 1775504520000L,
)

@Composable
private fun FakeCatchDetailScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        DefaultAppBar(
            title = sampleCatch.fishType,
            subtitle = "${samplePlace.title} · Sat, 5:42 AM",
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CatchTitleView(catch = sampleCatch, onClick = {})
            ItemUserPlace(
                place = samplePlace,
                userPlaceClicked = {},
                navigateToMap = {},
            )
            DefaultNoteView(note = sampleCatch.note, onClick = {})
            WayOfFishingView(catch = sampleCatch, onClick = {})
        }
    }
}

@Preview(
    name = "Catch detail · marketing",
    device = Devices.PIXEL_7,
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
fun CatchDetailMarketingPreview() {
    MarketingFrame(
        headline = "Log every catch.",
        subline = "Species, weight, place, and the exact conditions you hooked it.",
    ) {
        FakeCatchDetailScreen()
    }
}
