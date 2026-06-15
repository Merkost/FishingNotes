package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mobileprism.fishing.ui.theme.IconSize
import com.mobileprism.fishing.ui.theme.Spacing

enum class AppTextStyle {
    Display, Heading, Title, Subtitle, Body, BodySmall, Caption, Support
}

internal fun textStyleFor(style: AppTextStyle, typography: Typography): TextStyle = when (style) {
    AppTextStyle.Display -> typography.displaySmall
    AppTextStyle.Heading -> typography.headlineSmall
    AppTextStyle.Title -> typography.titleLarge
    AppTextStyle.Subtitle -> typography.titleMedium
    AppTextStyle.Body -> typography.bodyLarge
    AppTextStyle.BodySmall -> typography.bodyMedium
    AppTextStyle.Caption -> typography.bodySmall
    AppTextStyle.Support -> typography.labelMedium
}

@Composable
@ReadOnlyComposable
private fun AppTextStyle.toTextStyle(): TextStyle = textStyleFor(this, FishingTheme.typography)

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
    color: Color = FishingTheme.colorScheme.onSurfaceVariant,
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
            modifier = Modifier.size(IconSize.md),
        )
        AppText(text = text, style = style, color = color)
    }
}
