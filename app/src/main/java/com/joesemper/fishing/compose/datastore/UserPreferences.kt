package com.joesemper.fishing.compose.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joesemper.fishing.compose.ui.theme.AppThemeValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userSettings")

        val USER_LOCATION_PERMISSION_KEY = booleanPreferencesKey("should_show_location_permission")
        val TIME_FORMAT_KEY = booleanPreferencesKey("use_12h_time_format")
        val APP_THEME_KEY = stringPreferencesKey("app_theme")
    }

    //get the saved value
    val shouldShowLocationPermission: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] ?: true
        }

    val use12hTimeFormat: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TIME_FORMAT_KEY] ?: false
        }

    val appTheme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[APP_THEME_KEY] ?: AppThemeValues.Blue.name

        }

    //save values
    suspend fun saveLocationPermissionStatus(shouldShow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] = shouldShow
        }
    }

    suspend fun saveTimeFormatStatus(use12hFormat: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TIME_FORMAT_KEY] = use12hFormat
        }
    }

    suspend fun saveAppTheme(appTheme: AppThemeValues) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = appTheme.name
        }
    }


}