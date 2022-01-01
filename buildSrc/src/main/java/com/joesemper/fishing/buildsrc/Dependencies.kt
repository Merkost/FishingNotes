package com.joesemper.fishing.buildsrc

object Versions {
    const val ktlint = "0.41.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.3"

    object Accompanist {
        private const val version = "0.22.0-rc"
        const val insets = "com.google.accompanist:accompanist-insets:$version"
        const val systemuicontroller =
            "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val flowlayouts = "com.google.accompanist:accompanist-flowlayout:$version"
        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerindicators = "com.google.accompanist:accompanist-pager-indicators:$version"
        const val permissions = "com.google.accompanist:accompanist-permissions:$version"
        const val appCompatTheme = "com.google.accompanist:accompanist-appcompat-theme:$version"
    }

    object GoogleMaps {
        const val maps = "com.google.android.libraries.maps:maps:3.1.0-beta"
        const val mapsKtx = "com.google.maps.android:maps-v3-ktx:3.3.0"
    }

    object PlayServices {
        private const val version = "19.0.0"
        private const val oldVersion = "18.0.1"

        const val auth = "com.google.android.gms:play-services-auth:$version"
        const val ads = "com.google.android.gms:play-services-ads:20.4.0"

        //Maps
        const val maps = "com.google.android.gms:play-services-maps:$oldVersion"
        const val location = "com.google.android.gms:play-services-location:$version"
    }

    object Kotlin {
        private const val version = "1.6.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.6.0"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.7.0"
        const val splashScreen = "androidx.core:core-splashscreen:1.0.0-alpha02"

        object Compose {
            const val newVersion = "1.1.0-rc02"
            const val version = "1.1.0-rc01"
            const val oldVersion = "1.0.5"

            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"
            const val ui = "androidx.compose.ui:ui:${version}"
            const val uiUtil = "androidx.compose.ui:ui-util:${version}"
            const val runtime = "androidx.compose.runtime:runtime:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val animation = "androidx.compose.animation:animation:${version}"

            //Theme
            const val theme = "com.google.android.material:compose-theme-adapter:1.1.2"
            const val lottie = "com.airbnb.android:lottie-compose:4.2.2"

            // Tooling support (Previews, etc.)
            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"

            // UI Tests
            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"
        }

        object Activity {
            // Integration with activities
            const val activityCompose = "androidx.activity:activity-compose:1.4.0"
        }

        object Lifecycle {
            //1.0.0-alpha07
            // Integration with ViewModels
            const val viewModelCompose =
                "androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0"
        }

        object Navigation {
            const val version = "2.4.0-rc01"
            const val navigationCompose = "androidx.navigation:navigation-compose:$version"
        }

        object ConstraintLayout {
            const val constraintLayoutCompose =
                "androidx.constraintlayout:constraintlayout-compose:1.0.0-rc02"
        }

        object Datastore {
            const val datastorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
        }

        object DependencyInjection {
            private const val koin_version = "3.1.4"

            const val koinMain = "io.insert-koin:koin-android:${koin_version}"
            const val koinJava = "io.insert-koin:koin-android-compat:${koin_version}"
            const val koinWorkManager = "io.insert-koin:koin-androidx-workmanager:${koin_version}"
            const val koinCompose = "io.insert-koin:koin-androidx-compose:${koin_version}"
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
        private const val version = "20.0.2"

        // Import the Firebase BoM
        const val platform = "com.google.firebase:firebase-bom:29.0.3"

        // When using the BoM, you don't specify versions in Firebase library dependencies
        const val auth = "com.google.firebase:firebase-auth-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val storage = "com.google.firebase:firebase-storage-ktx"
        const val core = "com.google.firebase:firebase-core"

        const val firestore = "com.google.firebase:firebase-firestore-ktx:24.0.0"
        const val authUi = "com.firebaseui:firebase-ui-auth:8.0.0"


    }

    object JUnit {
        private const val version = "4.13"
        const val junit = "junit:junit:$version"
    }

    object Coil {
        const val coilCompose = "io.coil-kt:coil-compose:2.0.0-alpha06"
    }

    object Shimmer {
        const val shimmer = "me.vponomarenko:compose-shimmer:1.0.0"
    }

    object Retrofit {
        const val main = "com.squareup.retrofit2:retrofit:2.9.0"
        const val converterGson = "com.squareup.retrofit2:converter-gson:2.9.0"
        const val coroutinesAdapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
        const val logging = "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3"
    }
}
