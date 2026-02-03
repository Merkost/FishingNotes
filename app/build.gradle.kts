import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.firebasePerf)
    alias(libs.plugins.secretsGradlePlugin)
}

android {
    compileSdk = 36

    val keystorePropertiesFile = rootProject.file("local.properties")
    val keystoreProperties = Properties().apply {
        if (keystorePropertiesFile.exists()) {
            load(keystorePropertiesFile.inputStream())
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("fishing.jks")
//            storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD", "")
//            keyAlias = keystoreProperties.getProperty("KEY_ALIAS", "")
//            keyPassword = keystoreProperties.getProperty("KEY_PASSWORD", "")
        }
    }

    defaultConfig {
        namespace = "com.mobileprism.fishing"
        minSdk = 24
        targetSdk = 36
        versionCode = 16
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.firebase.bom))



    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    implementation("id.zelory:compressor:3.0.1")

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.performance)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.authUi)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.coroutines)

    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    implementation(libs.googleMaps.mapsKtx)
    implementation(libs.googleMaps.mapUtilsKtx)

    implementation(libs.playServices.maps)
    implementation(libs.playServices.location)
    implementation(libs.playServices.ads)
    implementation(libs.playServices.update)
    implementation(libs.playServices.billing)
    implementation(libs.playServices.auth)

    implementation(libs.androidX.coreKtx)
    implementation(libs.androidX.splashScreen)
    implementation(libs.activity.compose)
    implementation(libs.viewModel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.constraintLayout.compose)
    implementation(libs.datastorePreferences)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.layout)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiUtil)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation)
    implementation(libs.compose.iconsExtended)
    implementation(libs.compose.tooling)
    implementation(libs.compose.lottie)

    implementation(libs.koin.main)
    implementation(libs.koin.java)
    implementation(libs.koin.workManager)
    implementation(libs.koin.compose)

    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pagerIndicators)
    implementation(libs.accompanist.permissions)

    implementation(libs.accompanist.placeholder)

    implementation(libs.coil.compose)
    implementation(libs.foundation.layout.android)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidX.testCore)
    androidTestImplementation(libs.androidX.testRunner)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.androidX.testRules)
    androidTestImplementation(libs.androidX.testExtJunit)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.compose.uiTest)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.retrofitCoroutinesAdapter)
    implementation(libs.okhttpLoggingInterceptor)

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

    // Mockito
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.mockito:mockito-inline:4.4.0")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.mockito")
    }

    // Robolectric
    testImplementation("org.robolectric:robolectric:4.7.3")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test.ext:truth:1.4.0")
    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.test.espresso:espresso-intents:3.4.0")
}