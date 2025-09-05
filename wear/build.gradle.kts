plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.fitapp.wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitapp.wear"
        minSdk = 26  // Wear OS 2.0 minimum
        targetSdk = 34
        versionCode = 8
        versionName = "1.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/licenses/**",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*"
            )
        }
    }
}

dependencies {
    // Compose BOM for version management
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    
    // Wear OS Compose
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)
    
    // Wear OS UI enhancements
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)
    
    // Google Play Services for Wearable Data Layer
    implementation(libs.play.services.wearable)
    
    // Shared dependencies from main app
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    
    // Room database (shared with main app)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Core Android
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)
    
    // Material Icons
    implementation(libs.compose.material.icons)
}