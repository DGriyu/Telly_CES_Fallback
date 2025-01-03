import java.io.FileInputStream
import java.util.Properties

plugins {
    kotlin("plugin.serialization") version "1.6.21"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.telly_ces_fallback"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.telly_ces_fallback"
        minSdk = 30
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }

        buildConfigField("String", "ELEVEN_LABS_API_KEY", "\"${properties.getProperty("ELEVEN_LABS_API_KEY", "")}\"")
        buildConfigField("String", "ELEVEN_LABS_AGENT_ID", "\"${properties.getProperty("ELEVEN_LABS_AGENT_ID", "")}\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-Xopt-in=in=androidx.media3.common.util.UnstableApi"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.room.ktx)
    implementation(libs.volley)
    // AndroidX Compose BOM - Compatible with Android 11
    val composeBom = platform("androidx.compose:compose-bom:2023.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // AndroidX UI Test - Compatible with Compose BOM 2023.06.01
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.3")

    // Kotlin Standard Library - Compatible with Android 11
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")

    // AndroidX Core KTX - Compatible with Android 11
    implementation("androidx.core:core-ktx:1.12.0") // Verify if a newer patch exists within 1.12.x

    // Lifecycle Runtime KTX - Compatible with Android 11
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Activity Compose - Compatible with Android 11
    implementation("androidx.activity:activity-compose:1.7.2")

    // AppCompat - Compatible with Android 11
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Compose UI Libraries - Compatible with Compose BOM 2023.06.01
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-graphics:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")

    // Compose Material3 - Compatible with Compose BOM 2023.06.01
    implementation("androidx.compose.material3:material3:1.1.1")

    // Debug Implementations for Compose - Compatible with Compose BOM 2023.06.01
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.3")

    // AndroidX TV - Latest Alpha Version Compatible with Android 11
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")

    // ViewModel and Lifecycle - Compatible with Android 11
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    // Coil for GIFs - Compatible with Android 11
    implementation("io.coil-kt:coil:2.2.2")
    implementation("io.coil-kt:coil-gif:2.2.2")
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Kotlinx Serialization JSON - Compatible with Android 11
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    // Hilt Libraries - Compatible with Android 11
    implementation("com.google.dagger:hilt-android:2.46")
    kapt("com.google.dagger:hilt-compiler:2.46")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("androidx.work:work-runtime:2.8.1")

    // OkHttp for WebSocket - Compatible with Android 11
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation ("com.airbnb.android:lottie:6.6.2")
    implementation ("com.airbnb.android:lottie-compose:6.6.2")
    implementation ("com.teevee.sdk:telly-partner-sdk:3.0.2")
}