package com.mobileprism.fishing.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mobileprism.fishing.domain.use_cases.IosGeocoder
import com.mobileprism.fishing.domain.use_cases.PlatformGeocoder
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.UserPreferencesImpl
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.datastore.impl.NotesPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.UserDatastoreImpl
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.utils.location.IosLocationManager
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.IosConnectionManager
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val appModule = module {
    single<PlatformGeocoder> { IosGeocoder() }
    single<LocationManager> { IosLocationManager() }
}

actual val settingsModule = module {
    single<UserDatastore> { UserDatastoreImpl(createIosPreferencesDataStore("appSettings")) }
    single { UserPreferencesImpl(createIosPreferencesDataStore("userSettings")) }
    single<UserPreferences> { get<UserPreferencesImpl>() }
    single<WeatherPreferences> { WeatherPreferencesImpl(createIosPreferencesDataStore("weatherSettings")) }
    single<NotesPreferences> { NotesPreferencesImpl(createIosPreferencesDataStore("notesSettings")) }
    single<ConnectionManager> { IosConnectionManager() }
}

@OptIn(ExperimentalForeignApi::class)
private fun createIosPreferencesDataStore(name: String) =
    PreferenceDataStoreFactory.createWithPath {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )!!
        "${documentDirectory.path}/datastore/$name.preferences_pb".toPath()
    }
