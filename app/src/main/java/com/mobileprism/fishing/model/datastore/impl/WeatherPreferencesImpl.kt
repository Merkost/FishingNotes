package com.mobileprism.fishing.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface WeatherPreferences {
    fun getPressureUnit(): Flow<PressureValues>
    fun getTemperatureUnit(): Flow<TemperatureValues>
    fun getWindSpeedUnit(): Flow<WindSpeedValues>
    suspend fun savePressureUnit(pressureValues: PressureValues)
    suspend fun saveTemperatureUnit(temperatureValues: TemperatureValues)
    suspend fun saveWindSpeedUnit(windSpeedValues: WindSpeedValues)
}

class WeatherPreferencesImpl(private val context: Context) : WeatherPreferences {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("weatherSettings")

        val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_UNIT = stringPreferencesKey("wind_unit")
    }

    //get the saved value
    val getPressureUnit: Flow<PressureValues> = context.dataStore.data
        .map { preferences ->
            PressureValues.valueOf(preferences[PRESSURE_UNIT] ?: PressureValues.mmHg.name)
        }.catch { e ->
            if (e is IllegalArgumentException) { emit(PressureValues.mmHg) }
        }

    val getTemperatureUnit: Flow<TemperatureValues> = context.dataStore.data
        .map { preferences ->
            TemperatureValues.valueOf(preferences[TEMPERATURE_UNIT] ?: TemperatureValues.C.name)
        }.catch { e ->
            if (e is IllegalArgumentException) {
                emit(TemperatureValues.C)
            }
        }

    val getWindSpeedUnit: Flow<WindSpeedValues> = context.dataStore.data
        .map { preferences ->
            WindSpeedValues.valueOf(preferences[WIND_UNIT] ?: WindSpeedValues.metersps.name)
        }.catch { e ->
            if (e is IllegalArgumentException) {
                emit(WindSpeedValues.metersps)
            }
        }

    override fun getPressureUnit() = getPressureUnit

    override fun getTemperatureUnit() = getTemperatureUnit

    override fun getWindSpeedUnit() = getWindSpeedUnit

    //save into datastore
    override suspend fun savePressureUnit(pressureValues: PressureValues) {
        context.dataStore.edit { preferences ->
            preferences[PRESSURE_UNIT] = pressureValues.name
        }
    }

    override suspend fun saveTemperatureUnit(temperatureValues: TemperatureValues) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = temperatureValues.name
        }
    }

    override suspend fun saveWindSpeedUnit(windSpeedValues: WindSpeedValues) {
        context.dataStore.edit { preferences ->
            preferences[WIND_UNIT] = windSpeedValues.name
        }
    }

}