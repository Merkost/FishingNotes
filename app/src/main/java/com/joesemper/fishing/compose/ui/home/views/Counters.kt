package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor

@Composable
fun MaxCounterView(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    count: Int = 0,
    maxCount: Int = 0
) {
    val tint = animateColorAsState(
        targetValue = if (count > maxCount) Color.Red else secondaryTextColor
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp),
                painter = icon,
                tint = tint.value,
                contentDescription = null
            )
        }
        SecondaryText(
            text = "$count/$maxCount",
            textColor = tint.value

        )

    }
}