package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.mobileprism.fishing.ui.theme.IconSize
import com.mobileprism.fishing.ui.theme.Spacing

enum class AppButtonStyle { Filled, Tonal, Outlined, Text }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Filled,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: Painter? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(IconSize.sm),
                    strokeWidth = Spacing.xxs,
                    color = LocalContentColor.current,
                )
            } else if (leadingIcon != null) {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.sm),
                )
            }
            Text(text = text, maxLines = 1)
        }
    }

    val effectiveEnabled = enabled && !loading

    when (style) {
        AppButtonStyle.Filled -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }

        AppButtonStyle.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }

        AppButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }

        AppButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }
    }
}
