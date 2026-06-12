package com.mobileprism.fishing.utils.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberConnectionState(): State<ConnectionState> =
    remember { mutableStateOf(ConnectionState.Available) }
