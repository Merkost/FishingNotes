package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

enum class AppTextStyle {
    Display, Heading, Title, Subtitle, Body, BodySmall, Caption, Support
}

@Composable
@ReadOnlyComposable
private fun AppTextStyle.toTextStyle(): TextStyle = when (this) {
    AppTextStyle.Display -> MaterialTheme.typography.displaySmall
    AppTextStyle.Heading -> MaterialTheme.typography.headlineSmall
    AppTextStyle.Title -> MaterialTheme.typography.titleLarge
    AppTextStyle.Subtitle -> MaterialTheme.typography.titleMedium
    AppTextStyle.Body -> MaterialTheme.typography.bodyLarge
    AppTextStyle.BodySmall -> MaterialTheme.typography.bodyMedium
    AppTextStyle.Caption -> MaterialTheme.typography.bodySmall
    AppTextStyle.Support -> MaterialTheme.typography.labelMedium
}

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.Body,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.toTextStyle(),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun TextWithLeadingIcon(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.Subtitle,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentDescription: String? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        AppText(text = text, style = style, color = color)
    }
}
