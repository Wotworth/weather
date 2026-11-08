// WX-4.6, CI only: a stand-in for the Weather app's tile provider, serving fixed days, so the
// launcher's CI can draw the tile without an AccuWeather key. Never released or installed on a phone.
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.wotworth.weather.fake"
    compileSdk = 37

    defaultConfig {
        // The real app's package (its provider authority is in the manifest).
        applicationId = "io.github.wotworth.weather"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "ci"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
