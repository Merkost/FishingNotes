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

fun resolveProperty(key: String, default: String = ""): String =
    localProperties.getProperty(key) ?: secretsProperties.getProperty(key) ?: default

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.firebasePerf)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.mobileprism.fishing.android"
    compileSdk = 37

    signingConfigs {
        create("release") {
            val keystorePath = resolveProperty("KEYSTORE_PATH")
            val storePass = resolveProperty("KEYSTORE_PASSWORD")
            storeFile = if (keystorePath.isNotBlank()) rootProject.file(keystorePath) else rootProject.file("fishing.jks")
            storePassword = storePass
            keyAlias = resolveProperty("KEY_ALIAS")
            keyPassword = resolveProperty("KEY_PASSWORD").ifBlank { storePass }
        }
    }

    defaultConfig {
        applicationId = "com.merkost.fishingnotes"
        minSdk = 24
        targetSdk = 36
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
        manifestPlaceholders["MAPS_API_KEY"] = resolveProperty("MAPS_API_KEY")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
}
