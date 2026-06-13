package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.mobileprism.fishing.ui.theme.customColors

@Composable
fun MaxCounterView(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    count: Int = 0,
    maxCount: Int = 0
) {
    val tint = animateColorAsState(
        targetValue = if (count > maxCount) MaterialTheme.colorScheme.error else MaterialTheme.customColors.secondaryTextColor
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
        AppText(
            text = "$count/$maxCount",
            style = AppTextStyle.Body,
            color = tint.value,
            textAlign = TextAlign.Center
        )

    }
}