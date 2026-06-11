package com.mobileprism.fishing.marketing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.home.map.PlaceDetailsCardContent
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.map_marketing
import org.jetbrains.compose.resources.painterResource

private data class FakeMarker(
    val name: String,
    val xFraction: Float,
    val yFraction: Float,
    val color: Color,
)

private val sampleMarkers = listOf(
    FakeMarker("Lake Tinaroo", 0.32f, 0.28f, Color(0xFFE53935)),
    FakeMarker("Atherton Dam", 0.55f, 0.40f, Color(0xFFF57C00)),
    FakeMarker("Barron River", 0.20f, 0.55f, Color(0xFF43A047)),
    FakeMarker("Cairns Inlet", 0.68f, 0.62f, Color(0xFF1E88E5)),
    FakeMarker("Lake Eacham", 0.40f, 0.78f, Color(0xFF8E24AA)),
)

@Composable
private fun FakeMapScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.map_marketing),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth
            val h = maxHeight
            sampleMarkers.forEach { marker ->
                Box(
                    modifier = Modifier.offset(
                        x = w * marker.xFraction - 18.dp,
                        y = h * marker.yFraction - 18.dp,
                    ),
                ) {
                    MarkerPin(color = marker.color)
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            PlaceDetailsCardContent(
                title = "Lake Tinaroo",
                address = "Atherton Tablelands, QLD",
                distance = "1.2 km",
                subtitleLoading = false,
                temperatureCelsius = 24,
                weatherIconName = "02d",
                fishActivityPercent = 78,
                windSpeedText = "8 km/h",
                windRotationDeg = 45f,
                catchesCount = 12,
                showAddCatch = true,
                onCardClick = {},
                onAddCatchClick = {},
            )
        }
    }
}

@Composable
private fun MarkerPin(color: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Preview(
    name = "Map · marketing",
    device = Devices.PIXEL_7,
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
fun MapMarketingPreview() {
    MarketingFrame(
        headline = "Mark every spot.",
        subline = "Pin your favorite waters and never forget where the bite was on.",
    ) {
        FakeMapScreen()
    }
}
