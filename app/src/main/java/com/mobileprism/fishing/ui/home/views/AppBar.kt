package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R

@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    elevation: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colors.primary,
    actions: @Composable() (RowScope.() -> Unit) = {}

) {
    var navBack: @Composable (() -> Unit)? = null
    if (onNavClick != null) {
        navBack = {
            IconButton(onClick = onNavClick) {
                Icon(
                    imageVector = navIcon,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    }

    TopAppBar(
        modifier = modifier.statusBarsPadding(),
        title = {
            Column {
                Text(text = title)
                if (subtitle != null) {
                    SecondaryTextSmall(
                        text = subtitle,
                        textColor = MaterialTheme.colors.onPrimary
                    )
                }
            }
        },
        navigationIcon = navBack,
        elevation = elevation,
        actions = actions,
        backgroundColor = backgroundColor
    )

}