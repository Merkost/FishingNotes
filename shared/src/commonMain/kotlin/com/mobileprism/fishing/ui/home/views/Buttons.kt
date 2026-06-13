package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.customColors

@Composable
fun DefaultButton(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.tertiary,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        enabled = enabled,
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp),
                painter = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            text = text.uppercase(),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun DefaultButtonSecondaryLight(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor,
    onClick: () -> Unit
) {
    DefaultButton(
        modifier = modifier,
        icon = icon,
        text = text,
        enabled = enabled,
        textColor = textColor,
        onClick = onClick
    )
}

@Composable
fun DefaultButtonOutlined(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val color = animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.customColors.secondaryIconColor
        }
    )

    TextButton(
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(width = 1.dp, color = color.value),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp),
                painter = it,
                contentDescription = null,
                tint = color.value
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            text = text.uppercase(),
            color = color.value,
            maxLines = 1
        )
    }
}

@Composable
fun DefaultButtonFilled(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp),
                painter = it,
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            text = text.uppercase(),
            maxLines = 1
        )
    }
}

@Composable
fun DefaultIconButton(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    icon: Painter,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(
            modifier = childModifier.size(24.dp),
            painter = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}