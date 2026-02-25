package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.theme.customColors

@Composable
fun SubtitleWithIcon(modifier: Modifier = Modifier, icon: Int, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.place),
            tint = MaterialTheme.customColors.secondaryTextColor,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp)
        )
        SubtitleText(text = text)
    }
}
