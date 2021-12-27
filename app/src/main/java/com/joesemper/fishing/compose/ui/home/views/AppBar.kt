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
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R

@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: () -> Unit,
    title: String,
    subtitle: String? = null,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
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
        navigationIcon = {
            IconButton(onClick = { onNavClick() }) {
                Icon(
                    imageVector = navIcon,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        elevation = 4.dp,
        actions = actions
    )
}