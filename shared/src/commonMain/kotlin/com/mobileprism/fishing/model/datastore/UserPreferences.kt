package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.ui.home.map.MapCameraState
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    val shouldShowLocationPermission: Flow<Boolean>
    val use12hTimeFormat: Flow<Boolean>
    val appTheme: Flow<AppThemeValues>
    val darkMode: Flow<DarkModeValues>
    val useFabFastAdd: Flow<Boolean>
    val useMapZoomButons: Flow<Boolean>
    val shouldShowHiddenPlacesOnMap: Flow<Boolean>
    val getLastMapCameraLocation: Flow<MapCameraState>

    suspend fun saveLocationPermissionStatus(shouldShow: Boolean)
    suspend fun saveTimeFormatStatus(use12hFormat: Boolean)
    suspend fun saveAppTheme(appTheme: AppThemeValues)
    suspend fun saveDarkMode(darkMode: DarkModeValues)
    suspend fun saveFabFastAdd(fastAdd: Boolean)
    suspend fun saveMapZoomButtons(useZoomButtons: Boolean)
    suspend fun saveMapHiddenPlaces(shouldShow: Boolean)
    suspend fun saveLastMapCameraLocation(cameraState: MapCameraState)

    val hasCompletedOnboarding: Flow<Boolean>
    val hasPromptCardDismissed: Flow<Boolean>
    suspend fun saveOnboardingCompleted(completed: Boolean)
    suspend fun savePromptCardDismissed(dismissed: Boolean)
}
