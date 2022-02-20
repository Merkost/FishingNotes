package com.mobileprism.fishing.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.compose.ui.home.map.DEFAULT_ZOOM
import com.mobileprism.fishing.compose.ui.utils.enums.AppThemeValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userSettings")

        val LAST_MAP_LATITUDE = doublePreferencesKey("last_map_camera_latitude")
        val LAST_MAP_LONGITUDE = doublePreferencesKey("last_map_camera_longitude")
        val LAST_MAP_ZOOM = floatPreferencesKey("last_map_camera_zoom")

        val USER_LOCATION_PERMISSION_KEY = booleanPreferencesKey("should_show_location_permission")
        val MAP_HIDDEN_PLACES_KEY = booleanPreferencesKey("should_show_hidden_places_on_map")
        val TIME_FORMAT_KEY = booleanPreferencesKey("use_12h_time_format")
        val FAB_FAST_ADD = booleanPreferencesKey("fab_fast_add")
        val MAP_ZOOM_BUTTONS_KEY = booleanPreferencesKey("map_zoom_buttons")
        val APP_THEME_KEY = stringPreferencesKey("app_theme")
    }

    //get the saved value
    val shouldShowLocationPermission: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] ?: true
        }

    val shouldShowHiddenPlacesOnMap: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MAP_HIDDEN_PLACES_KEY] ?: true
        }

    val use12hTimeFormat: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TIME_FORMAT_KEY] ?: false
        }

    val appTheme: Flow<AppThemeValues> = context.dataStore.data
        .map { preferences ->
            AppThemeValues.valueOf(preferences[APP_THEME_KEY] ?: AppThemeValues.Blue.name)
        }.catch { e ->
            if (e is IllegalArgumentException) {
                emit(AppThemeValues.Blue)
            }
        }

    val useFabFastAdd: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FAB_FAST_ADD] ?: false
        }

    val useMapZoomButons: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MAP_ZOOM_BUTTONS_KEY] ?: false
        }

    val getLastMapCameraLocation: Flow<Pair<LatLng, Float>> = context.dataStore.data
        .map { preferences ->
            Pair(
                LatLng(
                    preferences[LAST_MAP_LATITUDE] ?: 0.0,
                    preferences[LAST_MAP_LONGITUDE] ?: 0.0
                ),
                preferences[LAST_MAP_ZOOM] ?: DEFAULT_ZOOM
            )
        }

    //save values
    suspend fun saveLocationPermissionStatus(shouldShow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] = shouldShow
        }
    }

    suspend fun saveMapHiddenPlaces(shouldShow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MAP_HIDDEN_PLACES_KEY] = shouldShow
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

    suspend fun saveFabFastAdd(fastAdd: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FAB_FAST_ADD] = fastAdd
        }
    }

    suspend fun saveMapZoomButtons(useZoomButtons: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MAP_ZOOM_BUTTONS_KEY] = useZoomButtons
        }
    }

    suspend fun saveLastMapCameraLocation(pair: Pair<LatLng, Float>) {
        context.dataStore.edit { preferences ->
            preferences[LAST_MAP_LATITUDE] = pair.first.latitude
            preferences[LAST_MAP_LONGITUDE] = pair.first.longitude
            preferences[LAST_MAP_ZOOM] = pair.second
        }
    }

}