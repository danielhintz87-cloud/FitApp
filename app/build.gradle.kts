apply(plugin = "com.android.application")
apply(plugin = "kotlin-android")
apply(plugin = "kotlinx-serialization")
apply(plugin = "kotlin-kapt")

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

    buildFeatures { 
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.4.6" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2023.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // Insets padding
    implementation("androidx.compose.foundation:foundation")

    // Photo Picker
    implementation("androidx.activity:activity-compose:1.7.2")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Coil for images
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Room database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
}
