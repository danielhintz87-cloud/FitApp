plugins {
    id("com.android.application")
    kotlin("android")
    // Required for Kotlin 2.0 Compose compiler
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "2.0.20"
    kotlin("kapt")
}

android {
    namespace = "com.example.fitapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // API Keys aus gradle.properties -> BuildConfig
        buildConfigField("String", "OPENAI_API_KEY", "\"${project.findProperty("OPENAI_API_KEY") ?: ""}\"")
        buildConfigField("String", "OPENAI_BASE_URL", "\"${project.findProperty("OPENAI_BASE_URL") ?: "https://api.openai.com"}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${project.findProperty("DEEPSEEK_API_KEY") ?: ""}\"")
        buildConfigField("String", "DEEPSEEK_BASE_URL", "\"${project.findProperty("DEEPSEEK_BASE_URL") ?: "https://api.deepseek.com"}\"")
    }

    buildFeatures { compose = true
    buildConfig = true
}
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Insets padding
    implementation("androidx.compose.foundation:foundation")

    // Markdown (leichtgewichtig)
    implementation("io.noties.markwon:core:4.6.2")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // CameraX (FoodScan)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // Coil für Bilder
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}
