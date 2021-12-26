package com.joesemper.fishing.compose.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joesemper.fishing.compose.ui.home.weather.PressureValues
import com.joesemper.fishing.compose.ui.home.weather.TemperatureValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class WeatherPreferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("weatherSettings")

        val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }

    //get the saved value
    val getPressureUnit: Flow<PressureValues> = context.dataStore.data
        .map { preferences ->
            PressureValues.valueOf(preferences[PRESSURE_UNIT] ?: PressureValues.mmHg.name)
        }.catch { e ->
            if (e is IllegalArgumentException) {
                emit(PressureValues.mmHg)
            }
        }

    val getTemperatureUnit: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TEMPERATURE_UNIT] ?: TemperatureValues.C.name
        }

    //save into datastore
    suspend fun savePressureUnit(pressureValues: PressureValues) {
        context.dataStore.edit { preferences ->
            preferences[PRESSURE_UNIT] = pressureValues.name
        }
    }


    suspend fun saveTemperatureUnit(temperatureValues: TemperatureValues) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = temperatureValues.name
        }
    }

}