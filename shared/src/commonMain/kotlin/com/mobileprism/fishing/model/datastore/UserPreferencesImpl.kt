package com.mobileprism.fishing.model.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mobileprism.fishing.ui.home.map.MapCameraState
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesImpl(private val dataStore: DataStore<Preferences>) : UserPreferences {

    companion object {
        val LAST_MAP_LATITUDE = doublePreferencesKey("last_map_camera_latitude")
        val LAST_MAP_LONGITUDE = doublePreferencesKey("last_map_camera_longitude")
        val LAST_MAP_ZOOM = floatPreferencesKey("last_map_camera_zoom")
        val LAST_MAP_BEARING = floatPreferencesKey("last_map_camera_bearing")

        val USER_LOCATION_PERMISSION_KEY = booleanPreferencesKey("should_show_location_permission")
        val MAP_HIDDEN_PLACES_KEY = booleanPreferencesKey("should_show_hidden_places_on_map")
        val TIME_FORMAT_KEY = booleanPreferencesKey("use_12h_time_format")
        val FAB_FAST_ADD = booleanPreferencesKey("fab_fast_add")
        val MAP_ZOOM_BUTTONS_KEY = booleanPreferencesKey("map_zoom_buttons")
        val APP_THEME_KEY = stringPreferencesKey("app_theme")
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
    }

    override val shouldShowLocationPermission: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] ?: true
        }

    override val shouldShowHiddenPlacesOnMap: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[MAP_HIDDEN_PLACES_KEY] ?: true
        }

    override val use12hTimeFormat: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[TIME_FORMAT_KEY] ?: false
        }

    override val appTheme: Flow<AppThemeValues> = dataStore.data
        .map { preferences ->
            AppThemeValues.valueOf(preferences[APP_THEME_KEY] ?: AppThemeValues.Blue.name)
        }.catch { e ->
            if (e is IllegalArgumentException) {
                emit(AppThemeValues.Blue)
            }
        }

    override val darkMode: Flow<DarkModeValues> = dataStore.data
        .map { preferences ->
            DarkModeValues.valueOf(preferences[DARK_MODE_KEY] ?: DarkModeValues.System.name)
        }.catch { e ->
            if (e is IllegalArgumentException) {
                emit(DarkModeValues.System)
            }
        }

    override val useFabFastAdd: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[FAB_FAST_ADD] ?: false
        }

    override val useMapZoomButons: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[MAP_ZOOM_BUTTONS_KEY] ?: false
        }

    override val getLastMapCameraLocation: Flow<MapCameraState> = dataStore.data
        .map { preferences ->
            MapCameraState(
                latitude = preferences[LAST_MAP_LATITUDE] ?: 0.0,
                longitude = preferences[LAST_MAP_LONGITUDE] ?: 0.0,
                zoom = preferences[LAST_MAP_ZOOM] ?: 0f,
                bearing = preferences[LAST_MAP_BEARING] ?: 0f,
            )
        }

    override suspend fun saveLocationPermissionStatus(shouldShow: Boolean) {
        dataStore.edit { preferences ->
            preferences[USER_LOCATION_PERMISSION_KEY] = shouldShow
        }
    }

    override suspend fun saveMapHiddenPlaces(shouldShow: Boolean) {
        dataStore.edit { preferences ->
            preferences[MAP_HIDDEN_PLACES_KEY] = shouldShow
        }
    }

    override suspend fun saveTimeFormatStatus(use12hFormat: Boolean) {
        dataStore.edit { preferences ->
            preferences[TIME_FORMAT_KEY] = use12hFormat
        }
    }

    override suspend fun saveAppTheme(appTheme: AppThemeValues) {
        dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = appTheme.name
        }
    }

    override suspend fun saveDarkMode(darkMode: DarkModeValues) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode.name
        }
    }

    override suspend fun saveFabFastAdd(fastAdd: Boolean) {
        dataStore.edit { preferences ->
            preferences[FAB_FAST_ADD] = fastAdd
        }
    }

    override suspend fun saveMapZoomButtons(useZoomButtons: Boolean) {
        dataStore.edit { preferences ->
            preferences[MAP_ZOOM_BUTTONS_KEY] = useZoomButtons
        }
    }

    override suspend fun saveLastMapCameraLocation(cameraState: MapCameraState) {
        dataStore.edit { preferences ->
            preferences[LAST_MAP_LATITUDE] = cameraState.latitude
            preferences[LAST_MAP_LONGITUDE] = cameraState.longitude
            preferences[LAST_MAP_ZOOM] = cameraState.zoom
            preferences[LAST_MAP_BEARING] = cameraState.bearing
        }
    }
}
