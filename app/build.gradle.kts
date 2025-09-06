import java.util.Properties
import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose) // Compose plugin alias aus deiner libs.versions.toml
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.example.fitapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitapp"
        minSdk = 26  // Health Connect kompatibel
        targetSdk = 34
        versionCode = 8
        versionName = "1.8"

        // API-Keys aus local.properties lesen (Fallback: leer)
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val perplexityApiKey = localProperties.getProperty("PERPLEXITY_API_KEY") ?: ""

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "PERPLEXITY_API_KEY", "\"$perplexityApiKey\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("debugMinified") {
            initWith(getByName("debug"))
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            applicationIdSuffix = ".debug.minified"
            versionNameSuffix = "-debug-minified"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf("debug")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        mlModelBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    lint {
        checkDependencies = false
        warningsAsErrors = false
        abortOnError = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    // Room Schema-Export für Migrationstests
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }

    kotlin {
        jvmToolchain(17)
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
    implementation(libs.tensorflow.lite.metadata)
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // AppCompat (Theming-Kompatibilität)
    implementation(libs.appcompat)

    // Material Icons
    implementation(libs.compose.material.icons)

    // Navigation
    implementation(libs.navigation.compose)

    // Activity (Compose + Photo Picker)
    implementation(libs.activity.compose)
    implementation(libs.activity.ktx)

    // Lifecycle/Coroutines
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room (AI-Logs)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Networking & JSON
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // Retrofit Stack (OpenFoodFacts)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    // Coil (Bildvorschau)
    implementation(libs.coil.compose)

    // CameraX (Barcode and ML)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // MediaPipe Tasks - Pose Landmarker
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode.scanning)

    // TensorFlow Lite (Advanced ML)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.5.0")

    // ONNX Runtime (Optional)
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.16.3")

    // Health Connect (Aktivitäts-/Kaloriensync)
    implementation(libs.health.connect.client)

    // Wearable Data Layer (Wear OS Kommunikation)
    implementation(libs.play.services.wearable)

    // WorkManager (Hintergrundjobs)
    implementation(libs.work.runtime.ktx)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Unit Tests
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.5.0")

    // Instrumented Tests
    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.android.test.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    // Debug Test Manifest
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}

// Jacoco
jacoco {
    toolVersion = "0.8.8"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**",
        "**/android/databinding/**",
        "**/androidx/databinding/**",
        "**/*_MembersInjector.class",
        "**/Dagger*Component*.class",
        "**/*Module_*Factory.class",
        "**/di/module/*",
        "**/*_Factory*.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug")
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree.exclude(fileFilter)))
    executionData.setFrom(fileTree(layout.buildDirectory.get()).include("jacoco/testDebugUnitTest.exec"))
}

// ==== Models Copy Task ====
val modelsRoot = rootProject.layout.projectDirectory.dir("models")
val appAssets = layout.projectDirectory.dir("src/main/assets/models")
val appMl = layout.projectDirectory.dir("src/main/ml")

tasks.register("copyModels") {
    doFirst {
        println(
            "Kopiere Modelle in: ${appAssets.asFile.absolutePath} und ${appMl.asFile.absolutePath}"
        )
    }
    doLast {
        project.copy {
            from(modelsRoot.dir("tflite")) { into("tflite") }
            from(modelsRoot.dir("onnx")) { into("onnx") }
            into(appAssets)
        }
        project.copy {
            from(modelsRoot.dir("tflite"))
            into(appMl)
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn("copyModels")
}
