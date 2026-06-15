package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.theme.IconSize
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.utils.time.toDate

@Composable
fun DefaultNoteView(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: () -> Unit,
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_baseline_sticky_note_2_24),
        title = stringResource(Res.string.note),
        onClick = onClick
    ) {
        if (note.description.isEmpty()) {
            NoContentView(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.no_description),
                icon = painterResource(Res.drawable.ic_no_note)
            )
        } else {
            LabeledValueRow(
                label = note.dateCreated.toDate(),
                value = note.description,
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
        }
    }
}

@Composable
fun NoContentView(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(IconSize.lg),
            painter = icon,
            contentDescription = null,
            tint = FishingTheme.colorScheme.onSurfaceVariant
        )
        AppText(
            text = text,
            style = AppTextStyle.Body,
            color = FishingTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherIconItem(
    iconResource: DrawableResource,
    iconTint: Color = Color.Unspecified,
    requiredSize: Dp = 50.dp,
    onIconSelected: (() -> Unit)? = null
) {
    val clickableModifier = onIconSelected?.let { Modifier.clickable { onIconSelected() } } ?: Modifier
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(FishingTheme.shapes.medium)
            .requiredSize(requiredSize)
            .then(clickableModifier)
    ) {
        Icon(painterResource(iconResource), "", tint = iconTint)
    }
}

@Composable
fun WindIconItem(
    iconTint: Color = FishingTheme.colorScheme.tertiary,
    rotation: Float,
    onIconSelected: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(FishingTheme.shapes.medium)
            .requiredSize(50.dp)
            .clickable(onClick = onIconSelected)
    ) {
        Icon(
            modifier = Modifier.rotate(rotation),
            painter = painterResource(Res.drawable.ic_baseline_navigation_24),
            contentDescription = null,
            tint = iconTint
        )
    }
}


