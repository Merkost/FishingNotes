package com.mobileprism.fishing.ui.home.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mobileprism.fishing.ui.theme.customColors

@Composable
fun BigText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall,
        textAlign = textAlign,
        color = textColor,
        text = text
    )
}

@Composable
fun SubtitleText(
    modifier: Modifier = Modifier, text: String,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor, maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Start
) {

    Text(
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = textColor,
        text = text,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        )
}

@Composable
fun PrimaryText(
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = fontWeight,
        textAlign = textAlign,
        color = textColor,
        text = text,
        maxLines = maxLines,
        softWrap = true,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun PrimaryTextSmall(
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        color = textColor,
        text = text
    )
}

@Composable
fun SecondaryTextColored(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        style = style,
        color = color,
        text = text,
        maxLines = maxLines,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor
) {
    Text(
        textAlign = textAlign,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        text = text,
        maxLines = maxLines
    )
}

@Composable
fun SecondaryTextSmall(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor
) {
    Text(
        textAlign = textAlign,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = textColor,
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SupportText(
    modifier: Modifier = Modifier, text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        style = style,
        color = MaterialTheme.customColors.secondaryTextColor,
        text = text,
        maxLines = 1
    )
}

