plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.roborazzi)
}

// versionName is 0.<phase>.<run>; versionCode is the build workflow's run number (as Wotworth/Tennis).
val phase = 1
val buildNumber = (System.getenv("VERSION_CODE") ?: "1").toInt()
val buildDate = System.getenv("BUILD_DATE") ?: "local build"

// WX-4.5: release signing comes only from the CI secrets. Without them the release APK is unsigned.
val releaseKeyFile: String? = System.getenv("KEYSTORE_FILE")
val releaseKeyPassword: String? = System.getenv("KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("KEY_ALIAS")

android {
    namespace = "io.github.wotworth.weather"
    compileSdk = 37

    defaultConfig {
        // WX-1.1: never change it (the launcher and the tile provider depend on it).
        applicationId = "io.github.wotworth.weather"
        minSdk = 33
        targetSdk = 37
        versionCode = buildNumber
        versionName = "0.$phase.$buildNumber"
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
    }

    signingConfigs {
        if (releaseKeyFile != null && releaseKeyPassword != null && releaseKeyAlias != null) {
            create("upload") {
                storeFile = file(releaseKeyFile)
                storePassword = releaseKeyPassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                storeType = "pkcs12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("upload")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
            it.jvmArgs("--add-opens=java.base/java.io=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            // Cloud sessions only: fetch Robolectric's Android runtime from a Maven Central mirror.
            System.getenv("ROBOLECTRIC_REPO_URL")?.let { url -> it.systemProperty("robolectric.dependency.repo.url", url) }
        }
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

roborazzi {
    outputDir.set(file("build/outputs/roborazzi"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.work.runtime)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.work.testing)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.junit.rule)
}
