package com.mobileprism.fishing.model.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class NotesPreferencesImpl(private val dataStore: DataStore<Preferences>) : NotesPreferences {

    companion object {
        private val PLACES_SORT_KEY = stringPreferencesKey("places_sort")
        private val CATCHES_SORT_KEY = stringPreferencesKey("catches_sort")
    }

    override val getPlacesSortValue: Flow<PlacesSortValues> = dataStore.data
        .map { preferences ->
            PlacesSortValues.valueOf(preferences[PLACES_SORT_KEY] ?: PlacesSortValues.TimeAsc.name)
        }.catch { emit(PlacesSortValues.TimeAsc) }

    override val getCatchesSortValue: Flow<CatchesSortValues> = dataStore.data
        .map { preferences ->
            CatchesSortValues.valueOf(preferences[CATCHES_SORT_KEY] ?: CatchesSortValues.TimeAsc.name)
        }.catch { emit(CatchesSortValues.TimeAsc) }

    override suspend fun savePlacesSortValue(placesSortValue: PlacesSortValues) {
        dataStore.edit { preferences ->
            preferences[PLACES_SORT_KEY] = placesSortValue.name
        }
    }

    override suspend fun saveCatchesSortValue(catchesSortValue: CatchesSortValues) {
        dataStore.edit { preferences ->
            preferences[CATCHES_SORT_KEY] = catchesSortValue.name
        }
    }
}
