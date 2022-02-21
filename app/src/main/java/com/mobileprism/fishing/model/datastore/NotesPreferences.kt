package com.mobileprism.fishing.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobileprism.fishing.ui.utils.enums.CatchesSortValues
import com.mobileprism.fishing.ui.utils.enums.PlacesSortValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class NotesPreferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("notesSettings")

        private val PLACES_SORT_KEY = stringPreferencesKey("places_sort")
        private val CATCHES_SORT_KEY = stringPreferencesKey("catches_sort")
    }

    //get the saved value
    val placesSortValue: Flow<PlacesSortValues> = context.dataStore.data
        .map { preferences ->
            PlacesSortValues.valueOf(preferences[PLACES_SORT_KEY] ?: PlacesSortValues.TimeAsc.name)
        }.catch { e ->
            if (e is IllegalArgumentException) { emit(PlacesSortValues.TimeAsc) }
        }

    val catchesSortValue: Flow<CatchesSortValues> = context.dataStore.data
        .map { preferences ->
            CatchesSortValues.valueOf(preferences[CATCHES_SORT_KEY] ?: CatchesSortValues.TimeAsc.name)
        }.catch { e ->
            if (e is IllegalArgumentException) { emit(CatchesSortValues.TimeAsc) }
        }


    //save values
    suspend fun savePlacesSortValue(placesSortValue: PlacesSortValues) {
        context.dataStore.edit { preferences ->
            preferences[PLACES_SORT_KEY] = placesSortValue.name
        }
    }

    suspend fun saveCatchesSortValue(catchesSortValues: CatchesSortValues) {
        context.dataStore.edit { preferences ->
            preferences[CATCHES_SORT_KEY] = catchesSortValues.name
        }
    }

}