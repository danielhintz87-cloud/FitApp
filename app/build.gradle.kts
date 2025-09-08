plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.example.fitapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitapp"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // BuildConfig fields for API keys
        buildConfigField("String", "GEMINI_API_KEY", "\"\"")
        buildConfigField("String", "PERPLEXITY_API_KEY", "\"\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.camera.core.ExperimentalGetImage"
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android Bundle
    implementation(libs.bundles.core)
    
    // Compose Bundle
    implementation(libs.bundles.compose)
    
    // Room Database Bundle
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Networking Bundle  
    implementation(libs.bundles.networking)

    // DataStore - KORREKTE DOT NOTATION!
    // DataStore Bundle
    implementation(libs.bundles.data.storage)
    implementation(libs.protobuf.kotlin.lite)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Networking Bundle (includes Retrofit & Moshi)
    implementation(libs.bundles.networking)
    ksp(libs.moshi.codegen)

    // Coil - KORREKTE DOT NOTATION!
    implementation(libs.coil.compose)

    // CameraX Bundle
    implementation(libs.bundles.camera)

    // MediaPipe
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // Browser Support
    implementation(libs.browser)

    // ML Bundle
    implementation(libs.bundles.ml)
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")

    // Health Connect
    implementation(libs.health.connect.client)

    // Wearable Data Layer
    implementation(libs.play.services.wearable)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Test Bundles
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    debugImplementation(libs.bundles.debug)
    
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.9.0")
}

// Protobuf configuration
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                // Remove kotlin generation to avoid duplicate symbols
            }
        }
    }
}
