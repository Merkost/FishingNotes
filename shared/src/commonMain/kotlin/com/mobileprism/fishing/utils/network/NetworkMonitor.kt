package com.mobileprism.fishing.utils.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
expect fun rememberConnectionState(): State<ConnectionState>
