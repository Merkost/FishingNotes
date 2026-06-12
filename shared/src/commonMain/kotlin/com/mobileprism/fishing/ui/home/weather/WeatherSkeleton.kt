package com.mobileprism.fishing.ui.home.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.components.SkeletonBox
import com.mobileprism.fishing.ui.components.SkeletonLine
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun WeatherSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBox(
            width = 360.dp,
            height = 320.dp,
            shape = RoundedCornerShape(20.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            repeat(5) {
                SkeletonBox(width = 56.dp, height = 80.dp)
            }
        }

        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    SkeletonLine(fraction = 0.4f, height = 16.dp)
                    SkeletonLine(fraction = 0.25f)
                }
                SkeletonBox(width = 40.dp, height = 40.dp)
                SkeletonLine(fraction = 0.2f, height = 16.dp)
            }
        }
    }
}
