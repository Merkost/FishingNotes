object Versions {
    const val ktlint = "0.41.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.3"
    const val jdkDesugar = "com.android.tools:desugar_jdk_libs:1.1.5"

    object Accompanist {
        private const val version = "0.28.0"
        const val systemuicontroller = "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val flowlayouts = "com.google.accompanist:accompanist-flowlayout:$version"
        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerindicators = "com.google.accompanist:accompanist-pager-indicators:$version"
        const val permissions = "com.google.accompanist:accompanist-permissions:$version"
        const val appCompatTheme = "com.google.accompanist:accompanist-appcompat-theme:$version"
        const val placeholder = "com.google.accompanist:accompanist-placeholder-material:$version"
        const val navigationComposeAnimated = "com.google.accompanist:accompanist-navigation-animation:$version"

    }

    object GoogleMaps {
        //KTX for the Maps SDK for Android library
        // KTX for the Maps SDK for Android Utility Library
        //"com.google.android.libraries.maps:maps:3.1.0-beta"
        const val maps = "com.google.maps.android:maps-compose:2.8.0"
        const val mapsKtx = "com.google.maps.android:maps-ktx:3.4.0"
        const val mapUtilsKtx = "com.google.maps.android:maps-utils-ktx:3.4.0"

        //Google maps distance between two latlng /*0.4.4*/
        const val mapUtils = "com.google.maps.android:android-maps-utils:2.3.0"

    }

    object PlayServices {
        private const val version = "20.4.0"
        private const val oldVersion = "18.1.0"

        const val auth = "com.google.android.gms:play-services-auth:$version"
        const val ads = "com.google.android.gms:play-services-ads:21.2.0"
        const val billing = "com.android.billingclient:billing-ktx:5.0.0"
        const val core = "com.google.android.play:core-ktx:1.8.1"

        //Maps
        const val maps = "com.google.android.gms:play-services-maps:$oldVersion"
        const val location = "com.google.android.gms:play-services-location:20.0.0"
    }

    object Kotlin {
        private const val version = "1.7.20"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.6.4"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.7.0"
        const val splashScreen = "androidx.core:core-splashscreen:1.0.0-beta01"

        object Compose {
            const val version = "2023.01.00"
            const val platform = "androidx.compose:compose-bom:$version"

            const val foundation = "androidx.compose.foundation:foundation"
            const val layout = "androidx.compose.foundation:foundation-layout"
            const val ui = "androidx.compose.ui:ui"
            const val uiUtil = "androidx.compose.ui:ui-util"
            const val runtime = "androidx.compose.runtime:runtime"
            const val material = "androidx.compose.material:material"
            const val animation = "androidx.compose.animation:animation"

            //Theme
            const val theme = "com.google.android.material:compose-theme-adapter:1.1.18"
            const val lottie = "com.airbnb.android:lottie-compose:5.2.0"

            // Tooling support (Previews, etc.)
            const val tooling = "androidx.compose.ui:ui-tooling"
            const val iconsExtended = "androidx.compose.material:material-icons-extended"

            // UI Tests
            const val uiTest = "androidx.compose.ui:ui-test-junit4"
        }

        object Activity {
            // Integration with activities
            const val activityCompose = "androidx.activity:activity-compose:1.4.0"
        }

        object Lifecycle {
            // Integration with ViewModels
            const val viewModelCompose =
                "androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0"
        }

        object Navigation {
            const val version = "2.4.0"
            const val navigationCompose = "androidx.navigation:navigation-compose:$version"
        }

        object ConstraintLayout {
            const val constraintLayoutCompose =
                "androidx.constraintlayout:constraintlayout-compose:1.0.0"
        }

        object Datastore {
            const val datastorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
        }

        object DependencyInjection {
            private const val koin_version = "3.2.0"

            const val koinMain = "io.insert-koin:koin-android:$koin_version"
            const val koinJava = "io.insert-koin:koin-android-compat:$koin_version"
            const val koinWorkManager = "io.insert-koin:koin-androidx-workmanager:$koin_version"
            const val koinCompose = "io.insert-koin:koin-androidx-compose:$koin_version"
        }

        object Test {
            private const val version = "1.4.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            object Ext {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit-ktx:$version"
            }

            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }
    }

    object Firebase {
        // Import the Firebase BoM
        const val platform = "com.google.firebase:firebase-bom:31.1.1"

        // When using the BoM, you don't specify versions in Firebase library dependencies
        const val auth = "com.google.firebase:firebase-auth-ktx"

        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val performance = "com.google.firebase:firebase-perf-ktx"
        const val storage = "com.google.firebase:firebase-storage-ktx"

        const val core = "com.google.firebase:firebase-core:21.1.1"

        const val firestore = "com.google.firebase:firebase-firestore-ktx"
//        const val authUi = "com.firebaseui:firebase-ui-auth:8.0.0"

        //Firebase Coroutine dependency:
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.1"

    }

    object JUnit {
        private const val version = "4.13.2"
        const val junit = "junit:junit:$version"
    }

    object Coil {
        const val coilCompose = "io.coil-kt:coil-compose:2.2.2"
    }

    object Room {
        private const val roomVersion = "2.4.3"

        const val roomRuntime = "androidx.room:room-runtime:$roomVersion"
        const val roomCompiler = "androidx.room:room-compiler:$roomVersion"
        const val roomKtx = "androidx.room:room-ktx:$roomVersion"
        const val roomTesting = "androidx.room:room-testing:$roomVersion"
    }

    object Retrofit {
        const val main = "com.squareup.retrofit2:retrofit:2.9.0"
        const val converterGson = "com.squareup.retrofit2:converter-gson:2.9.0"
        const val coroutinesAdapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
        const val logging = "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3"
    }
}
