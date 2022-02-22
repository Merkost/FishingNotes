package com.mobileprism.fishing.utils.network

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single

class ConnectionManagerImpl(private val context: Context) : ConnectionManager {

    override fun getConnectionStateFlow(): Flow<ConnectionState> {
        return context.observeConnectivityAsFlow()
    }

    override suspend fun getConnectionState(): ConnectionState {
        return context.observeConnectivityAsFlow().single()
    }

}