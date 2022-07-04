package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.model.entity.user.Token

interface TokenStore {
    suspend fun setToken(newToken: String)
    suspend fun getCurrentToken(): Token
}