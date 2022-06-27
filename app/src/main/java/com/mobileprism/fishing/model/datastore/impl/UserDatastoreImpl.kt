package com.mobileprism.fishing.model.datastore.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.datastore.UserDatastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserDatastoreImpl(private val context: Context): UserDatastore {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appSettings")

        private val USER_KEY = stringPreferencesKey("user")
    }

    //get the saved value
    override val getUser: Flow<User> = context.dataStore.data
        .map { preferences ->
            Gson().fromJson(preferences[USER_KEY], User::class.java) ?: User()
        }.catch { emit(User()) }

    //get the saved value
    override val getNullableUser: Flow<User> = context.dataStore.data
        .map { preferences ->
            Gson().fromJson(preferences[USER_KEY], User::class.java)
        }.catch { emit(null) }

    override suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = Gson().toJson(user)
        }
    }

    override suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = ""
        }
    }

}