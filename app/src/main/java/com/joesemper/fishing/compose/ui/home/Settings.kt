package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joesemper.fishing.R

@Composable
fun SettingsScreen(backPress: () -> Unit) {
    Scaffold (topBar = { SettingsTopAppBar(backPress) },
    modifier = Modifier.fillMaxSize()) {

    }
}

@Composable
fun SettingsTopAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(id = R.string.settings),
        onNavClick = { backPress() }
    ) {}
}
