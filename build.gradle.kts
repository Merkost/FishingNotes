apply(plugin = "com.github.ben-manes.versions")

buildscript {
    val kotlin_version = "1.7.0"

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools:r8:3.3.75")
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.google.gms:google-services:4.3.13")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")

        //gradle dependencyUpdates -Drevision=release -DoutputFormatter=json,xml,html
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")

        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")
        classpath("com.google.firebase:perf-plugin:1.4.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
        gradlePluginPortal()
    }

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

/*tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {

    // optional parameters
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}*/


