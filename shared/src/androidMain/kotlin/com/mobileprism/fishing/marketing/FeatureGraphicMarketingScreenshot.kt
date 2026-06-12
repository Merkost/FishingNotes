package com.mobileprism.fishing.marketing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource

@Composable
private fun FakeCoverScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B4F6C), Color(0xFF1B98E0)),
                ),
            ),
    ) {
        DecorativeBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .clip(RoundedCornerShape(44.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(32.dp)),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Fishing Notes",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your fishing log,\nalways with you.",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
            )

            Spacer(modifier = Modifier.height(36.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTag("Map")
                FeatureTag("Catches")
                FeatureTag("Weather")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTag("Sync")
                FeatureTag("Offline")
            }
        }
    }
}

@Composable
private fun FeatureTag(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DecorativeBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(280.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(220.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.05f)),
        )
    }
}

@Preview(
    name = "Cover · marketing",
    device = Devices.PIXEL_7,
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
fun CoverMarketingPreview() {
    MarketingFrame(
        headline = "Fishing Notes",
        subline = "The fishing log that goes everywhere you do.",
    ) {
        FakeCoverScreen()
    }
}

@Composable
fun FeatureGraphicLandscape() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0B4F6C), Color(0xFF1B98E0)),
                ),
            ),
    ) {
        DecorativeBackdrop()
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                )
            }
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Fishing Notes",
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mark your spots. Log every catch.\nCheck the weather before you go.",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureTag("Map")
                    FeatureTag("Catches")
                    FeatureTag("Weather")
                    FeatureTag("Offline")
                }
            }
        }
    }
}

@Preview(
    name = "Feature graphic · marketing",
    showBackground = true,
    widthDp = 512,
    heightDp = 250,
)
@Composable
private fun FeatureGraphicLandscapePreview() {
    FeatureGraphicLandscape()
}
