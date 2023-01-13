package com.mobileprism.fishing.di

import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionManagerImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
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


val networkModule = module {
    //Create HttpLoggingInterceptor
    single<HttpLoggingInterceptor> { createLoggingInterceptor() }

    //Create OkHttpClient
    single<OkHttpClient> { createOkHttpClient(get()) }

    //Create ConnectionManager
    single<ConnectionManager> { ConnectionManagerImpl(androidContext()) }
}


val fishingNetworkModule = module {
    //Create Fishing OkHttpClient
    single<OkHttpClient>(named("fishing_okhttp")) {
        createFishingOkHttpClient(androidContext(), get())
    }

    //Create Fishing Retrofit
    single<Retrofit>(named("fishing_retrofit")) {
        createFishingRetrofit(get(named("fishing_okhttp")))
    }
}


fun createFishingRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(Constants.API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create())
        .client(okHttpClient)
        .build()
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
        .hostnameVerifier { hostname, _ ->
            Constants.API_URL.contains(hostname)
        }
        .sslSocketFactory(
            sslSocketFactory = getSSLConfig(context).socketFactory,
            systemDefaultTrustManager()
        )
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