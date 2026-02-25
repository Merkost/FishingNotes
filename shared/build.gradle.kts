import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
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
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Empty for now — future shared code dependencies go here
        }

        androidMain.dependencies {
            api(libs.kotlin.stdlib)
            api(libs.coroutines.android)
            api(libs.coroutines.core)
            api(libs.androidX.coreKtx)
            api(libs.androidX.splashScreen)
            api(libs.activity.compose)
            api(libs.lifecycle.runtimeKtx)
            api(libs.navigation.compose)
            api(libs.compose.runtime)
            api(libs.compose.material3)
            api(libs.koin.main)
            api(libs.koin.workManager)
            api(libs.coil.compose)
            api(libs.firebase.auth)
            implementation(libs.kmpauth.google)
            api(libs.playServices.ads)
            api(libs.playServices.update)

            implementation(libs.work.runtimeKtx)
            implementation(libs.compressor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.performance)
            implementation(libs.firebase.authUi)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.coroutines)

            implementation(libs.googleMaps.mapsKtx)
            implementation(libs.googleMaps.mapUtilsKtx)
            implementation(libs.playServices.maps)
            implementation(libs.playServices.location)
            implementation(libs.playServices.billing)
            implementation(libs.playServices.auth)

            implementation(libs.viewModel.compose)
            implementation(libs.constraintLayout.compose)
            implementation(libs.datastorePreferences)

            implementation(libs.room.runtime)
            implementation(libs.room.ktx)

            implementation(libs.compose.foundation)
            implementation(libs.compose.layout)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiUtil)
            implementation(libs.compose.animation)
            implementation(libs.compose.iconsExtended)
            implementation(libs.compose.tooling)
            implementation(libs.compose.lottie)

            implementation(libs.koin.java)
            implementation(libs.koin.compose)

            implementation(libs.accompanist.pager)
            implementation(libs.accompanist.pagerIndicators)
            implementation(libs.accompanist.permissions)
            implementation(libs.accompanist.placeholder)

            implementation(libs.foundation.layout.android)
            implementation(libs.vico.compose.m3)

            implementation(libs.paging.runtime)
            implementation(libs.paging.compose)

            implementation(libs.retrofit)
            implementation(libs.retrofitConverterKotlinxSerialization)
            implementation(libs.retrofitCoroutinesAdapter)
            implementation(libs.okhttpLoggingInterceptor)
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

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }

    buildFeatures {
        compose = true
    }
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

buildkonfig {
    packageName = "com.mobileprism.fishing"
    this.exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "OPENWEATHER_KEY",
            localProperties.getProperty("OPENWEATHER_KEY", ""))
        buildConfigField(FieldSpec.Type.STRING, "RAPIDAPI_KEY",
            localProperties.getProperty("RAPIDAPI_KEY", ""))
        buildConfigField(FieldSpec.Type.STRING, "MAPS_API_KEY",
            localProperties.getProperty("MAPS_API_KEY", ""))
        buildConfigField(FieldSpec.Type.STRING, "GOOGLE_WEB_CLIENT_ID",
            localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", ""))
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    }

    targetConfigs("android") {
        create("release") {
            buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
        }
    }
}
