package com.joesemper.fishing.buildsrc

object Versions {
    const val ktlint = "0.41.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.0"

    object Accompanist {
        private const val version = "0.18.0"
        const val insets = "com.google.accompanist:accompanist-insets:$version"
        const val systemuicontroller = "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val flowlayouts = "com.google.accompanist:accompanist-flowlayout:$version"
        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerindicators = "com.google.accompanist:accompanist-pager-indicators:$version"
        const val permissions = "com.google.accompanist:accompanist-permissions:$version"
    }

    object GoogleMaps {
        const val maps = "com.google.android.libraries.maps:maps:3.1.0-beta"
        const val mapsKtx = "com.google.maps.android:maps-v3-ktx:2.2.0"
    }

    object Kotlin {
        private const val version = "1.5.21"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.5.2"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.6.0"

        object Compose {
            const val snapshot = ""
            const val version = "1.0.2"

            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"
            const val ui = "androidx.compose.ui:ui:${version}"
            const val uiUtil = "androidx.compose.ui:ui-util:${version}"
            const val runtime = "androidx.compose.runtime:runtime:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val animation = "androidx.compose.animation:animation:${version}"
            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"
            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"
        }

        object Activity {
            const val activityCompose = "androidx.activity:activity-compose:1.3.1"
        }

        object Lifecycle {
            const val viewModelCompose =
                "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"
        }

        object Navigation {
            const val navigationCompose = "androidx.navigation:navigation-compose:2.4.0-alpha07"
        }

        object ConstraintLayout {
            const val constraintLayoutCompose =
                "androidx.constraintlayout:constraintlayout-compose:1.0.0-beta02"
        }

        object DependencyInjection {
            private const val koin_version = "3.1.2"

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

    object JUnit {
        private const val version = "4.13"
        const val junit = "junit:junit:$version"
    }

    object Coil {
        const val coilCompose = "io.coil-kt:coil-compose:1.3.0"
    }
}
