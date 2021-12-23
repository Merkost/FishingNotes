package com.joesemper.fishing.compose.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joesemper.fishing.compose.ui.theme.AppThemeValues
import com.joesemper.fishing.compose.ui.utils.PlacesSortValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesPreferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("notesSettings")

        private val PLACES_SORT_KEY = stringPreferencesKey("places_sort")
        private val CATCHES_SORT_KEY = stringPreferencesKey("catches_sort")
    }

    //get the saved value
    val placesSortValue: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PLACES_SORT_KEY] ?: PlacesSortValues.TimeAsc.name
        }

    val catchesSortValue: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PLACES_SORT_KEY] ?: PlacesSortValues.TimeAsc.name
        }


    //save values
    suspend fun savePlacesSortValue(placesSortValue: String) {
        context.dataStore.edit { preferences ->
            preferences[PLACES_SORT_KEY] = placesSortValue
        }
    }

    suspend fun saveCatchesSortValue(placesSortValue: String) {
        context.dataStore.edit { preferences ->
            preferences[PLACES_SORT_KEY] = placesSortValue
        }
    }

}