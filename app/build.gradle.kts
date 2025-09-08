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

    // Compose - KORREKTE DOT NOTATION!
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // AppCompat
    implementation(libs.appcompat)

    // Material Icons - KORREKTE DOT NOTATION!
    implementation(libs.compose.material.icons)

    // Navigation - KORREKTE DOT NOTATION!
    implementation(libs.navigation.compose)

    // Activity - KORREKTE DOT NOTATION!
    implementation(libs.activity.compose)
    implementation(libs.activity.ktx)

    // Browser - KORREKTE DOT NOTATION!
    implementation(libs.browser)

    // Lifecycle/Coroutines - KORREKTE DOT NOTATION!
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore - KORREKTE DOT NOTATION!
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)
    implementation(libs.datastore.proto)
    implementation(libs.protobuf.kotlin.lite)

    // Room - KORREKTE DOT NOTATION!
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Networking & JSON - KORREKTE DOT NOTATION!
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // Retrofit Stack - KORREKTE DOT NOTATION!
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    // Coil - KORREKTE DOT NOTATION!
    implementation(libs.coil.compose)

    // CameraX - KORREKTE DOT NOTATION!
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // MediaPipe
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // ML Kit - KORREKTE DOT NOTATION!
    implementation(libs.mlkit.barcode.scanning)

    // TensorFlow Lite - KORREKTE DOT NOTATION!
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")

    // ONNX Runtime
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.22.0")

    // Health Connect - KORREKTE DOT NOTATION!
    implementation(libs.health.connect.client)

    // Wearable Data Layer - KORREKTE DOT NOTATION!
    implementation(libs.play.services.wearable)

    // WorkManager - KORREKTE DOT NOTATION!
    implementation(libs.work.runtime.ktx)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Desugaring - KORREKTE DOT NOTATION!
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    
    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.android.test.core)
    
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
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}
