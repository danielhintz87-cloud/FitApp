plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
    // Static analysis tools
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

// Ensure Hilt's annotation processors see the JavaPoet version that provides
// `ClassName.canonicalName()` to avoid runtime NoSuchMethodError during the
// aggregation task.
configurations.configureEach {
    resolutionStrategy.force("com.squareup:javapoet:1.13.0")
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
                "proguard-rules.pro",
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
        freeCompilerArgs +=
            listOf(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=androidx.camera.core.ExperimentalGetImage",
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

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        checkReleaseBuilds = false
        disable += "GradleDependency"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Module Dependencies
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))

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
    kapt(libs.hilt.compiler)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Test Bundles
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    debugImplementation(libs.bundles.debug)

    debugImplementation("androidx.compose.ui:ui-test-manifest:1.9.0")

    // Compose Lint Checks for accessibility - use version from BOM
    lintChecks("androidx.compose.ui:ui-lint")
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

hilt {
    enableAggregatingTask = false
}

// Static analysis configuration
detekt {
    source.setFrom("src/main/java", "src/main/kotlin")
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    autoCorrect = true
}

ktlint {
    version.set("1.0.1")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// NavGraph reachability check task
tasks.register("checkNavGraphReachability") {
    group = "verification"
    description = "Checks that all navigation destinations are reachable"

    doLast {
        val navGraphFiles =
            fileTree("src/main/res/navigation") {
                include("**/*.xml")
            }

        if (navGraphFiles.isEmpty) {
            println("No navigation graph files found - skipping reachability check")
            return@doLast
        }

        navGraphFiles.forEach { file ->
            println("Checking navigation graph: ${file.name}")
            val content = file.readText()

            // Basic check for unreferenced destinations
            val destinations =
                Regex("<(fragment|activity)\\s+[^>]*android:id=\"@\\+?id/([^\"]+)\"").findAll(content)
                    .map { it.groupValues[2] }.toSet()
            val actions =
                Regex("android:destination=\"@\\+?id/([^\"]+)\"").findAll(content)
                    .map { it.groupValues[1] }.toSet()

            val unreachable = destinations - actions
            if (unreachable.isNotEmpty()) {
                // Remove start destination from unreachable list
                val startDestination =
                    Regex(
                        "app:startDestination=\"@\\+?id/([^\"]+)\"",
                    ).find(content)?.groupValues?.get(1)
                val actualUnreachable =
                    if (startDestination != null) {
                        unreachable - startDestination
                    } else {
                        unreachable
                    }

                if (actualUnreachable.isNotEmpty()) {
                    throw GradleException("Unreachable destinations found in ${file.name}: $actualUnreachable")
                }
            }
            println("✓ Navigation graph ${file.name} is valid")
        }
    }
}
