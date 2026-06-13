package com.mobileprism.fishing.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.placeholder

@Composable
fun SkeletonBox(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape,
            )
    )
}

@Composable
fun SkeletonBox(
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .height(height)
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape,
            )
    )
}

@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    fraction: Float = 1f,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .fillMaxWidth(fraction)
            .height(height)
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape,
            )
    )
}

@Composable
fun CardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SkeletonLine(fraction = 0.5f, height = 18.dp)
        SkeletonLine(fraction = 0.9f)
        SkeletonLine(fraction = 0.7f)
    }
}

@Composable
fun ListItemSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBox(width = 48.dp, height = 48.dp, shape = RoundedCornerShape(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            SkeletonLine(fraction = 0.6f, height = 16.dp)
            SkeletonLine(fraction = 0.4f)
        }
    }
}
