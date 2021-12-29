package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R

@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    elevation: Dp = 4.dp,
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
        modifier = modifier,
        title = {
            Column() {
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
        actions = actions
    )
}