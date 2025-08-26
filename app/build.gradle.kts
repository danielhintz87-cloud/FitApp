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
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyBvqPCG17SaKJBRSghTpoEcs_EQfOfQVOs\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"sk-06acf33d0fa2411cafeb7eace7b0fd83\"")

        // Recommended base URLs
        buildConfigField("String", "OPENAI_BASE_URL", "\"https://api.openai.com\"")
        buildConfigField("String", "GEMINI_BASE_URL", "\"https://generativelanguage.googleapis.com\"")
        buildConfigField("String", "DEEPSEEK_BASE_URL", "\"https://api.deepseek.com\"")

        // Default model configurations
        buildConfigField("String", "OPENAI_MODEL", "\"gpt-4o\"")
        buildConfigField("String", "GEMINI_MODEL", "\"gemini-1.5-pro\"")
        buildConfigField("String", "DEEPSEEK_MODEL", "\"deepseek-chat\"")
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