import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val secretsProperties = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.detekt)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)                    // @Immutable
            implementation(compose.foundation)                 // CMP foundation
            implementation(compose.material3)                  // CMP Material3
            implementation(compose.ui)                         // CMP UI
            implementation(compose.materialIconsExtended)      // CMP Material Icons
            api(compose.components.resources)                   // CMP resource system (Res.string, Res.drawable)
            implementation(libs.coroutines.core)               // Flow, coroutines
            implementation(libs.kotlinx.serialization.json)    // @Serializable
            implementation(libs.kotlinx.datetime)              // Clock, Instant
            implementation(libs.koin.core)                     // Koin DI
            implementation(libs.koin.compose.viewmodel)          // Koin Compose ViewModel (KMP)
            implementation(libs.room.runtime)                   // Room KMP
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.firebase.gitlive.auth)
            implementation(libs.firebase.gitlive.firestore)
            implementation(libs.firebase.gitlive.storage)
            implementation(libs.lifecycle.viewmodel)             // KMP ViewModel
            api(libs.navigation.compose)                          // KMP Navigation
            implementation(libs.viewModel.compose)               // KMP ViewModel Compose
            api(libs.coil.compose)                               // Coil 3 KMP image loading
            api(libs.coil.network.ktor3)                         // Coil 3 Ktor network backend
            implementation(libs.vico.compose.m3)                   // Vico charts (KMP)
            implementation(libs.kmpMapsCompose.maps)                 // KMP Google Maps Compose
            api(libs.kmpauth.google)                                 // KMP Google Auth
            api(libs.cedar.logging)                                      // Cedar KMP logger
            implementation(libs.compottie)                                     // Compottie KMP Lottie
            implementation(libs.compottie.resources)                           // Compottie resources
            implementation(libs.paging.common)                           // Paging KMP
            implementation(libs.paging.compose)                          // Paging Compose KMP
            implementation(libs.datastore.preferences.core)              // DataStore Preferences KMP
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
            implementation(libs.mockk)
            implementation(libs.turbine)
            implementation(libs.koin.test)
        }

        androidMain.dependencies {
            api(libs.kotlin.stdlib)
            api(libs.coroutines.android)
            api(libs.coroutines.core)
            api(libs.androidX.coreKtx)
            api(libs.androidX.splashScreen)
            api(libs.activity.compose)
            api(libs.lifecycle.runtimeKtx)
            api(libs.compose.runtime)
            api(libs.compose.material3)
            api(libs.koin.main)
            api(libs.koin.workManager)
            api(libs.coil.network.okhttp)
            api(libs.playServices.ads)
            api(libs.playServices.update)

            implementation(libs.work.runtimeKtx)
            implementation(libs.compressor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.performance)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.coroutines)

            implementation(libs.playServices.maps)
            implementation(libs.playServices.location)
            implementation(libs.playServices.billing)
            implementation(libs.datastorePreferences)

            implementation(libs.compose.foundation)
            implementation(libs.compose.layout)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiUtil)
            implementation(libs.compose.animation)
            implementation(libs.compose.iconsExtended)
            implementation(libs.compose.tooling)


            implementation(libs.koin.java)
            implementation(libs.koin.compose)

            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.location)
            implementation(libs.foundation.layout.android)

            implementation(libs.ktor.client.okhttp)
        }
    }
}

android {
    namespace = "com.mobileprism.fishing"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }

    buildFeatures {
        compose = true
    }
}

compose.resources {
    publicResClass = true
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(platform(libs.compose.bom))
    api(platform(libs.firebase.bom))

    add("kspAndroid", libs.room.compiler)

    // Android instrumented tests
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidX.testCore)
    androidTestImplementation(libs.androidX.testRunner)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.androidX.testRules)
    androidTestImplementation(libs.androidX.testExtJunit)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.compose.uiTest)

}

buildkonfig {
    packageName = "com.mobileprism.fishing"
    this.exposeObjectWithName = "BuildKonfig"

    fun resolveProperty(key: String): String =
        localProperties.getProperty(key) ?: secretsProperties.getProperty(key) ?: ""

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "OPENWEATHER_KEY", resolveProperty("OPENWEATHER_KEY"))
        buildConfigField(FieldSpec.Type.STRING, "RAPIDAPI_KEY", resolveProperty("RAPIDAPI_KEY"))
        buildConfigField(FieldSpec.Type.STRING, "MAPS_API_KEY", resolveProperty("MAPS_API_KEY"))
        buildConfigField(FieldSpec.Type.STRING, "GOOGLE_WEB_CLIENT_ID", resolveProperty("GOOGLE_WEB_CLIENT_ID"))
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    }

    targetConfigs("android") {
        create("release") {
            buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
        }
    }
}
