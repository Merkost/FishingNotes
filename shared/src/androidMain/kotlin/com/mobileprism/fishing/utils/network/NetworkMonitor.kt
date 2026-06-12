package com.mobileprism.fishing.utils.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberConnectionState(): State<ConnectionState> {
    val context = LocalContext.current
    return context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)
}
