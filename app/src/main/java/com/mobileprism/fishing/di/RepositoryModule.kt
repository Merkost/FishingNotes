package com.mobileprism.fishing.di

import android.content.Context
import androidx.room.Room
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.*
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.*
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseOfflineRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.FishingDatabase
import com.mobileprism.fishing.model.datasource.room.LocalCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.LocalCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.room.LocalMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.room.dao.CatchesDao
import com.mobileprism.fishing.model.datasource.room.dao.MapMarkersDao
import com.mobileprism.fishing.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.IOException
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


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
            okHttpClient = createFishingOkHttpClient(androidContext(), createLoggingInterceptor()),
        )
    }

    single<FishRepository> {
        FishRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            okHttpClient = createFishingOkHttpClient(androidContext(), createLoggingInterceptor())
        )
    }

    single<SolunarRepository> { SolunarRetrofitRepositoryImpl(firebaseAnalytics = get()) }

    single<WeatherRepository> {
        WeatherRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            okHttpClient = createOkHttpClient(createLoggingInterceptor())
        )
    }

    single<FreeWeatherRepository> { FreeWeatherRepositoryImpl(firebaseAnalytics = get()) }

    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }

}

fun createLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().setLevel(
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    )
}

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

/**
 * Create a OkHttpClient which is used to send HTTP requests and read their responses.
 * @loggingInterceptor logging interceptor
 */
fun createFishingOkHttpClient(
    context: Context,
    loggingInterceptor: HttpLoggingInterceptor,
): OkHttpClient {

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(7, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .hostnameVerifier { hostname, session ->
            if (Constants.API_URL.contains(hostname)) true else false
        }
        .sslSocketFactory(sslSocketFactory = getSSLConfig(context).socketFactory, systemDefaultTrustManager())
        .build()
}

@Throws(
    CertificateException::class,
    IOException::class,
    KeyStoreException::class,
    NoSuchAlgorithmException::class,
    KeyManagementException::class
)
private fun getSSLConfig(context: Context): SSLContext {

    // Loading CAs from an InputStream
    val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
    var ca: Certificate
    context.resources.openRawResource(com.mobileprism.fishing.R.raw.fishing_cert).use { cert ->
        ca = cf.generateCertificate(cert)
    }

    // Creating a KeyStore containing our trusted CAs
    val keyStoreType: String = KeyStore.getDefaultType()
    val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
    keyStore.load(null, null)
    keyStore.setCertificateEntry("ca", ca)

    // Creating a TrustManager that trusts the CAs in our KeyStore.
    val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
    val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
    tmf.init(keyStore)

    // Creating an SSLSocketFactory that uses our TrustManager
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, tmf.trustManagers, null)
    return sslContext
}

private fun systemDefaultTrustManager(): X509TrustManager {
    return try {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            "Unexpected default trust managers:" + Arrays.toString(
                trustManagers
            )
        }
        trustManagers[0] as X509TrustManager
    } catch (e: GeneralSecurityException) {
        throw AssertionError() // The system has no TLS. Just give up.
    }
}
