package com.mobileprism.fishing.utils.network

import kotlinx.coroutines.flow.Flow

interface ConnectionManager {
    fun getConnectionStateFlow(): Flow<ConnectionState>
    suspend fun getConnectionState(): ConnectionState
}