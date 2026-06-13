package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.mobileprism.fishing.ui.utils.AnimatedResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.theme.customColors
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.customColors.secondaryIconColor
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.customColors.secondaryTextColor
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
            .clip(MaterialTheme.shapes.medium)
            .requiredSize(requiredSize)
            .then(clickableModifier)
    ) {
        Icon(painterResource(iconResource), "", tint = iconTint)
    }
}

@Composable
fun WindIconItem(
    iconTint: Color = MaterialTheme.colorScheme.tertiary,
    rotation: Float,
    onIconSelected: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
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

@Composable
fun NoInternetView(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedResource("error", modifier)
        SupportText(text = stringResource(Res.string.network_error_message))
        if (onRetry != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            DefaultButtonOutlined(
                text = stringResource(Res.string.retry),
                onClick = onRetry
            )
        }
    }
}

@Composable
fun ErrorView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(Res.drawable.ic_error), contentDescription = null
        )
        SupportText(text = stringResource(Res.string.something_went_wrong))
    }
}

