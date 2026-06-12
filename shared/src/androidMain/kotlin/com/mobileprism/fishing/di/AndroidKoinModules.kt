package com.mobileprism.fishing.di

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.mobileprism.fishing.domain.use_cases.AndroidGeocoder
import com.mobileprism.fishing.domain.use_cases.PlatformGeocoder
import com.mobileprism.fishing.model.datastore.*
import com.mobileprism.fishing.model.datastore.UserPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.NotesPreferencesImpl
import com.mobileprism.fishing.model.datastore.impl.UserDatastoreImpl
import com.mobileprism.fishing.model.datastore.impl.WeatherPreferencesImpl
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.location.LocationManagerImpl
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionManagerImpl
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val appModule = module {
    single<PlatformGeocoder> { AndroidGeocoder(createGeocoder(androidContext())) }
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
    single { LocationManagerImpl(get()) }
    single<LocationManager> { get<LocationManagerImpl>() }
}

actual val settingsModule = module {
    single<UserDatastore> { UserDatastoreImpl(createPreferencesDataStore(androidContext(), "appSettings")) }
    single { UserPreferencesImpl(createPreferencesDataStore(androidContext(), "userSettings")) }
    single<UserPreferences> { get<UserPreferencesImpl>() }
    single<WeatherPreferences> { WeatherPreferencesImpl(createPreferencesDataStore(androidContext(), "weatherSettings")) }
    single<NotesPreferences> { NotesPreferencesImpl(createPreferencesDataStore(androidContext(), "notesSettings")) }
    single<ConnectionManager> { ConnectionManagerImpl(androidContext()) }
}

fun createPreferencesDataStore(context: Context, name: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("datastore/$name.preferences_pb").absolutePath.toPath()
    }

fun createGeocoder(androidContext: Context): Geocoder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Geocoder(androidContext, androidContext.resources.configuration.locales[0])
    } else {
        Geocoder(androidContext)
    }
}
