package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    elevation: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    actions: @Composable (RowScope.() -> Unit) = {}

) {
    val contentColor = contentColorFor(backgroundColor)

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
                        textColor = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = navBack ?: {},
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
        )
    )

}
