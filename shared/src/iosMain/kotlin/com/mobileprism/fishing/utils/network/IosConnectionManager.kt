package com.mobileprism.fishing.utils.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class IosConnectionManager : ConnectionManager {
    override fun getConnectionStateFlow(): Flow<ConnectionState> =
        flowOf(ConnectionState.Available)

    override suspend fun getConnectionState(): ConnectionState =
        ConnectionState.Available
}
