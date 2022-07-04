package com.mobileprism.fishing.di

import androidx.room.Room
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.UserRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.FishingDatabase
import com.mobileprism.fishing.model.datasource.room.LocalCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.LocalCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.room.LocalMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.dao.CatchesDao
import com.mobileprism.fishing.model.datasource.room.dao.MapMarkersDao
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit


val repositoryModuleFirebase = module {

    single<CatchesRepository> {
        FirebaseCatchesRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            connectionManager = get()
        )
    }
    single<MarkersRepository> {
        FirebaseMarkersRepositoryImpl(
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }

}

val repositoryModuleLocal = module {

    single<FishingDatabase> {
        Room.databaseBuilder(get(), FishingDatabase::class.java, "Fishing.db")
            .fallbackToDestructiveMigration().build()
    }
    single<CatchesDao> { get<FishingDatabase>().catchesDao() }
    single<MapMarkersDao> { get<FishingDatabase>().mapMarkersDao() }


/*    single<FirebaseUserRepository> {
        LocalUserRepositoryImpl(
            userDatastore = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }*/

    single<CatchesRepository> {
        LocalCatchesRepositoryImpl(
            catchesDao = get(),
            firebaseAnalytics = get(),
        )
    }
    single<MarkersRepository> {
        LocalMarkersRepositoryImpl(
            markersDao = get(),
            firebaseAnalytics = get(),
        )
    }

    single<PhotoStorage> {
        LocalCloudPhotoStorage(
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }
    //todo

    single<UserRepository> {
        UserRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            okHttpClient = createOkHttpClient(createLoggingInterceptor()),
        )
    }
}

fun createLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().setLevel(
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    )
}

/**
 * Create a OkHttpClient which is used to send HTTP requests and read their responses.
 * @loggingInterceptor logging interceptor
 */
fun createOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(7, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .build()
}
