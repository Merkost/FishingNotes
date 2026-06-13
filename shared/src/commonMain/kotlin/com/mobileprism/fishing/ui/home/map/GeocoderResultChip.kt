package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.components.SkeletonLine
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.ic_baseline_location_on_24
import fishing.shared.generated.resources.marker_icon
import fishing.shared.generated.resources.searching
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GeocoderResultChip(
    result: GeocoderResult,
    placeName: String,
    modifier: Modifier = Modifier,
) {
    val isLoading = result is GeocoderResult.InProgress
    val isFailed = result is GeocoderResult.Failed

    val iconTint by animateColorAsState(
        targetValue = when {
            isFailed -> MaterialTheme.colorScheme.error
            placeName.isNotBlank() -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(250)
    )

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .heightIn(min = 40.dp, max = 80.dp)
            .widthIn(max = 240.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isFailed) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = stringResource(Res.string.marker_icon),
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                    contentDescription = stringResource(Res.string.marker_icon),
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.size(4.dp))
            if (isLoading) {
                SkeletonLine(
                    modifier = Modifier.widthIn(min = 80.dp, max = 160.dp),
                    height = 14.dp,
                )
            } else {
                Text(
                    text = placeName.ifEmpty { stringResource(Res.string.searching) },
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    color = if (isFailed) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            Spacer(Modifier.size(4.dp))
        }
    }
}
