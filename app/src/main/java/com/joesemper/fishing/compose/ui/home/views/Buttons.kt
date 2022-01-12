package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.compose.ui.theme.primaryTextColor

@Composable
fun DefaultButton(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    enabled: Boolean = true,
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
                tint = MaterialTheme.colors.primaryVariant
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            text = text.uppercase(),
            color = MaterialTheme.colors.primaryVariant,
            maxLines = 1
        )
    }
}

@Composable
fun DefaultButtonOutlined(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.primaryVariant),
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
                tint = MaterialTheme.colors.primaryVariant
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            text = text.uppercase(),
            color = MaterialTheme.colors.primaryVariant,
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
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.primaryVariant),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primaryVariant,
            contentColor = MaterialTheme.colors.onPrimary
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
    icon: Painter,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface
        )
    }
}