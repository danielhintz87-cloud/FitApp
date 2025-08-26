plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.kapt")
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

        // Direct API key placeholders - edit these values directly
        buildConfigField("String", "OPENAI_API_KEY", "\"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\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyDVx1Em9s5IcVm_9YVXKEn4Y7w7i5QqBrI\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"sk-a41a78a9e4354bb98c2b5a23c56070d7\"")

        // Recommended base URLs
        buildConfigField("String", "OPENAI_BASE_URL", "\"https://api.openai.com\"")
        buildConfigField("String", "GEMINI_BASE_URL", "\"https://generativelanguage.googleapis.com\"")
        buildConfigField("String", "DEEPSEEK_BASE_URL", "\"https://api.deepseek.com\"")

        // Default model configurations - Updated to latest free models
        buildConfigField("String", "OPENAI_MODEL", "\"gpt-4o\"")
        buildConfigField("String", "GEMINI_MODEL", "\"gemini-2.0-flash-exp\"")
        buildConfigField("String", "DEEPSEEK_MODEL", "\"deepseek-v3\"")
        

    }

    buildFeatures { 
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString() // "17"
        // freeCompilerArgs += listOf(/* falls du hier schon was hast, so lassen */)
    }

    kotlin {
        jvmToolchain(17)
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE*",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // AppCompat (for theme compatibility)
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material Icons (stabil & hübsch)
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // Activity (Compose + Photo Picker)
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.activity:activity-ktx:1.9.2")

    // Lifecycle/Coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Room (AI-Logs)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Networking & JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Coil (Vorschau des gewählten Bildes)
    implementation("io.coil-kt:coil-compose:2.7.0")
}