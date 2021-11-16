package com.joesemper.fishing.compose.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userSettings")

        val USER_LOCATION_PERMISSION_KEY = booleanPreferencesKey("should_show_location_permission")
    }

    //get the saved email
    val shouldShowLocationPermission: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] ?: true
        }

    //save email into datastore
    suspend fun saveLocationPermissionStatus(shouldShow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] = shouldShow
        }
    }


}