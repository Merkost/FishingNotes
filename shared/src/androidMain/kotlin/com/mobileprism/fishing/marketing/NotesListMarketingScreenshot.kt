package com.mobileprism.fishing.marketing

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.home.notes.CatchItemContent
import com.mobileprism.fishing.ui.home.notes.ItemDate
import com.mobileprism.fishing.ui.home.views.DefaultAppBar

private data class FakeCatchRow(
    val fishType: String,
    val weight: Double,
    val amount: Int,
    val place: String,
    val photos: Int,
    val time: String,
)

private val sampleCatches = listOf(
    FakeCatchRow("Barramundi", 4.8, 1, "Lake Tinaroo", 3, "05:42"),
    FakeCatchRow("Murray Cod", 6.2, 1, "Mitta Mitta", 5, "19:10"),
    FakeCatchRow("Flathead", 1.9, 2, "Cairns Inlet", 2, "06:12"),
    FakeCatchRow("Mangrove Jack", 2.4, 1, "Barron River", 4, "16:30"),
    FakeCatchRow("Threadfin Salmon", 3.6, 1, "Trinity Inlet", 1, "09:45"),
)

@Composable
private fun FakeNotesListScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FishingTheme.colorScheme.surface),
    ) {
        DefaultAppBar(
            title = "Notes",
            subtitle = "12 catches · 5 places",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 4.dp, vertical = 4.dp)),
        ) {
            ItemDate(text = "This week")
            sampleCatches.take(3).forEach { c ->
                CatchItemContent(
                    fishType = c.fishType,
                    fishWeight = c.weight,
                    fishAmount = c.amount,
                    placeTitle = c.place,
                    photoCount = c.photos,
                    timeText = c.time,
                    showPlace = true,
                    onClick = {},
                )
            }
            ItemDate(text = "Last week")
            sampleCatches.drop(3).forEach { c ->
                CatchItemContent(
                    fishType = c.fishType,
                    fishWeight = c.weight,
                    fishAmount = c.amount,
                    placeTitle = c.place,
                    photoCount = c.photos,
                    timeText = c.time,
                    showPlace = true,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(
    name = "Notes list · marketing",
    device = Devices.PIXEL_7,
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
fun NotesListMarketingPreview() {
    MarketingFrame(
        headline = "Your log, always with you.",
        subline = "Every catch, every place — synced and searchable across devices.",
    ) {
        FakeNotesListScreen()
    }
}
