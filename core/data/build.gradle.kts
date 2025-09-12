plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "de.hhn.fitapp.core.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
}