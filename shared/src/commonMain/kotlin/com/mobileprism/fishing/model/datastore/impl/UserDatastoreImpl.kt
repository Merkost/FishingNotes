package com.mobileprism.fishing.model.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.datastore.UserDatastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserDatastoreImpl(private val dataStore: DataStore<Preferences>) : UserDatastore {

    companion object {
        private val USER_KEY = stringPreferencesKey("user")
    }

    override val getUser: Flow<User> = dataStore.data
        .map { preferences ->
            preferences[USER_KEY]?.let {
                try { Json.decodeFromString<User>(it) } catch (_: Exception) { User() }
            } ?: User()
        }

    override suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_KEY] = Json.encodeToString(user)
        }
    }

    override suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }
}
