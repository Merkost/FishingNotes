package com.mobileprism.fishing.model.datastore.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mobileprism.fishing.model.datastore.TokenStore
import com.mobileprism.fishing.model.entity.user.Token
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TokenStoreImpl(private val context: Context) : TokenStore {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appData")

        private val TOKEN_KEY = stringPreferencesKey("token")
    }

    override suspend fun setToken(newToken: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = Gson().toJson(Token(newToken))
        }
    }

    override suspend fun getCurrentToken() = context.dataStore.data.map { preferences ->
        Gson().fromJson(preferences[TOKEN_KEY], Token::class.java) ?: Token()
    }.first()
}