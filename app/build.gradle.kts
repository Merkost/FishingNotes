plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")

    /*kotlin("android")
    kotlin("parcelize")
    kotlin("kapt")*/

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "com.mobileprism.fishing"
        minSdk = 23
        targetSdk = 33
        versionCode = 13
        versionName = "1.1.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += "room.schemaLocation" to "$projectDir/schemas".toString()
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            release {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
                )
            }
        }
        debug {
            ext {
                set("enableCrashlytics", false)
            }
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)

        // Flag to enable support for the new language APIs
        //coreLibraryDesugaringEnabled = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Libs.AndroidX.Compose.version
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    //Settings
    implementation("com.github.alorma:compose-settings-ui:0.11.0")

    implementation("androidx.work:work-runtime-ktx:2.7.1")

    //Compressor
    implementation("id.zelory:compressor:3.0.1")

    //coreLibraryDesugaring(Libs.jdkDesugar)

    implementation(Libs.Room.roomRuntime)
    kapt(Libs.Room.roomCompiler)
    implementation(Libs.Room.roomKtx)
    testImplementation(Libs.Room.roomTesting)

    implementation(platform(Libs.Firebase.platform))
    implementation(Libs.Firebase.core)
    implementation(Libs.Firebase.analytics)
    implementation(Libs.Firebase.crashlytics)
    implementation(Libs.Firebase.performance)
    implementation(Libs.Firebase.auth)
    implementation(Libs.Firebase.authUi)
    implementation(Libs.Firebase.firestore)
    implementation(Libs.Firebase.storage)
    implementation(Libs.Firebase.coroutines)

    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Coroutines.android)
    implementation(Libs.Coroutines.core)

    implementation(Libs.GoogleMaps.mapsKtx)
    implementation(Libs.GoogleMaps.mapUtilsKtx)

    implementation(Libs.PlayServices.maps)
    implementation(Libs.PlayServices.location)
    implementation(Libs.PlayServices.ads)
    implementation(Libs.PlayServices.core)
    implementation(Libs.PlayServices.billing)

    implementation(Libs.AndroidX.coreKtx)
    implementation(Libs.AndroidX.splashScreen)
    implementation(Libs.AndroidX.Activity.activityCompose)
    implementation(Libs.AndroidX.Lifecycle.viewModelCompose)
    implementation(Libs.AndroidX.Navigation.navigationCompose)
    implementation(Libs.AndroidX.ConstraintLayout.constraintLayoutCompose)
    implementation(Libs.AndroidX.Datastore.datastorePreferences)

    implementation(Libs.AndroidX.Compose.runtime)
    implementation(Libs.AndroidX.Compose.foundation)
    implementation(Libs.AndroidX.Compose.layout)
    implementation(Libs.AndroidX.Compose.ui)
    implementation(Libs.AndroidX.Compose.uiUtil)
    implementation(Libs.AndroidX.Compose.material)
    implementation(Libs.AndroidX.Compose.animation)
    implementation(Libs.AndroidX.Compose.iconsExtended)
    implementation(Libs.AndroidX.Compose.tooling)
    implementation(Libs.AndroidX.Compose.theme)
    implementation(Libs.AndroidX.Compose.lottie)

    implementation(Libs.AndroidX.DependencyInjection.koinMain)
    implementation(Libs.AndroidX.DependencyInjection.koinJava)
    implementation(Libs.AndroidX.DependencyInjection.koinWorkManager)
    implementation(Libs.AndroidX.DependencyInjection.koinCompose)

    implementation(Libs.Accompanist.insets)
    implementation(Libs.Accompanist.systemuicontroller)
    implementation(Libs.Accompanist.flowlayouts)
    implementation(Libs.Accompanist.pager)
    implementation(Libs.Accompanist.pagerindicators)
    implementation(Libs.Accompanist.permissions)
    implementation(Libs.Accompanist.appCompatTheme)
    implementation(Libs.Accompanist.placeholder)

    implementation(Libs.Coil.coilCompose)

    androidTestImplementation(Libs.JUnit.junit)
    androidTestImplementation(Libs.AndroidX.Test.core)
    androidTestImplementation(Libs.AndroidX.Test.runner)
    androidTestImplementation(Libs.AndroidX.Test.espressoCore)
    androidTestImplementation(Libs.AndroidX.Test.rules)
    androidTestImplementation(Libs.AndroidX.Test.Ext.junit)
    androidTestImplementation(Libs.Coroutines.test)
    androidTestImplementation(Libs.AndroidX.Compose.uiTest)

    //Retrofit
    implementation(Libs.Retrofit.main)
    implementation(Libs.Retrofit.converterGson)
    implementation(Libs.Retrofit.coroutinesAdapter)
    implementation(Libs.Retrofit.logging)

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    //Mockito
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.mockito:mockito-inline:4.4.0")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0") {
        exclude("org.jetbrains.kotlin")
        exclude("org.mockito")
    }

    //Robolectric
    testImplementation("org.robolectric:robolectric:4.7.3")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test.ext:truth:1.4.0")
    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.test.espresso:espresso-intents:3.4.0")
}
