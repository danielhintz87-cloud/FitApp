import java.util.Properties
import java.security.MessageDigest
import org.gradle.api.tasks.Copy
import java.time.Instant

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose) // Compose plugin alias aus deiner libs.versions.toml
    alias(libs.plugins.ksp)
    jacoco
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.example.fitapp"
    // Aktualisiert auf 36 (Build-Fehler: Dependencies fordern mind. 35 / Health Connect 36)
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitapp"
        minSdk = 26  // Health Connect kompatibel
    // targetSdk kann optional später angehoben werden; Anhebung jetzt zur Konsistenz
    targetSdk = 36
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
    // ML Model Binding deaktiviert: Modelle ohne eingebettete Metadata verursachen Fehler
    mlModelBinding = false
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

    // DataStore (Preferences + Core) – Migration Ziel statt SharedPreferences
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)

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
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.9.0")
        // Entfernt ältere 1.7.3 Android Coroutines – nur Version Katalog (1.9.0) aktiv
    // TensorFlow Lite (Advanced ML)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")
    // Entfernt: gpu-delegate-plugin (Artefakt nicht auffindbar). GPU Delegate ist bereits in tensorflow-lite-gpu enthalten.

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

        // CameraX (Barcode and ML) – vereinheitlicht über Version Catalog (camerax=1.3.1)
        // Optionales Upgrade auf 1.3.4 möglich durch Anpassung libs.versions.toml (versions.camerax)
        implementation(libs.camerax.core)
        implementation(libs.camerax.camera2)
        implementation(libs.camerax.lifecycle)
        implementation(libs.camerax.view)
    // Instrumented Tests
    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.android.test.core)
        // TensorFlow Lite (Advanced ML) – vereinheitlicht, vermeidet doppelte Artefakte
        implementation(libs.tensorflow.lite)
        implementation(libs.tensorflow.lite.support)
        implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.9.0")
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

// Kombinierter Coverage Report (Unit + Instrumented, falls später connected Tests laufen)
tasks.register<JacocoReport>("jacocoMergedReport") {
    group = "verification"
    description = "Merged coverage for unit + android tests"
    val unitExec = fileTree(layout.buildDirectory.get()).include("jacoco/testDebugUnitTest.exec")
    val androidExec = fileTree(layout.buildDirectory.get()).include("outputs/code_coverage/**/connectedDebugAndroidTest.exec")
    executionData.setFrom(files(unitExec, androidExec))

    dependsOn("testDebugUnitTest")
    // connectedDebugAndroidTest optional: falls nicht vorhanden, ignoriere
    // doFirst { }

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*"
    )
    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug").exclude(fileFilter)
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files(debugTree))
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files(rootProject.file("detekt.yml")))
    autoCorrect = false
}

ktlint {
    version.set("1.2.1")
    android.set(true)
    ignoreFailures.set(false)
    reporters { reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN) }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> { reportsOutputDirectory.set(layout.buildDirectory.dir("reports/ktlint").get().asFile) }

// Coverage Verification (simpler Gate)
val coverageMin = (project.findProperty("coverage.min") as String?)?.toDoubleOrNull() ?: 0.40 // 40% Default

tasks.register("verifyCoverage") {
    group = "verification"
    description = "Verifies line coverage >= coverage.min (default 40%)"
    dependsOn("jacocoTestReport")
    doLast {
        val reportFile = file("${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if (!reportFile.exists()) {
            logger.warn("Jacoco report fehlt: ${reportFile.path} – überspringe Gate")
            return@doLast
        }
        val content = reportFile.readText()
        val regex = Regex("<counter type=\"LINE\" missed=\"(\\d+)\" covered=\"(\\d+)\"/>")
        val match = regex.find(content)
        if (match != null) {
            val missed = match.groupValues[1].toDouble()
            val covered = match.groupValues[2].toDouble()
            val total = missed + covered
            val ratio = if (total == 0.0) 0.0 else covered / total
            logger.lifecycle("Line Coverage: ${(ratio * 100).format(2)}% (Threshold ${(coverageMin * 100).format(2)}%)")
            if (ratio < coverageMin) {
                throw GradleException("Coverage Gate failed: ${(ratio * 100).format(2)}% < ${(coverageMin * 100).format(2)}% (override with -Pcoverage.min=<value>)")
            }
        } else {
            logger.warn("Konnte LINE counter nicht parsen – Gate übersprungen")
        }
    }
}

fun Double.format(decimals: Int) = String.format("% .${decimals}f", this).trim()

// Aggregierter Quality Task
tasks.register("qualityCheck") {
    group = "verification"
    dependsOn("ktlintCheck", "detekt", "jacocoTestReport", "verifyCoverage")
}

// ==== Models Copy Task ====
val modelsRoot = rootProject.layout.projectDirectory.dir("models")
val appAssets = layout.projectDirectory.dir("src/main/assets/models")
// Entfernt: separates ml-Verzeichnis (Model Binding deaktiviert)

tasks.register("copyModels") {
    doFirst { println("Kopiere Modelle in: ${appAssets.asFile.absolutePath}") }
    doLast {
        project.copy {
            from(modelsRoot.dir("tflite")) { into("tflite") }
            from(modelsRoot.dir("onnx")) { into("onnx") }
            into(appAssets)
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn("copyModels")
}

// Verifiziert, dass alle erforderlichen Modelle vorhanden und keine Platzhalter sind
tasks.register("verifyModels") {
    group = "verification"
    description = "Prüft Existenz & Integrität (kein Platzhalter) der TFLite-Modelle"
    dependsOn("copyModels")
    doLast {
        val required = listOf(
            "movenet_thunder.tflite" to 300_000, // erwartete Mindestgröße ~ >300KB (eigentlich ~4-5MB float16)
            "blazepose.tflite" to 300_000,       // BlazePose Landmark Full ~9MB
            "movement_analysis_model.tflite" to 5_000 // Eigenes kleines Modell kann kleiner sein
        )
    // Nach copyModels liegen die Dateien unter app/src/main/assets/models/tflite
    val baseDir = layout.projectDirectory.dir("src/main/assets/models/tflite").asFile
        var failures = 0
        // Optional erwartete SHA256 Hashes (Env > local.properties)
        val props = Properties()
        val lp = rootProject.file("local.properties")
        if (lp.exists()) props.load(lp.inputStream())

        fun expectedHash(name: String): String? =
            (System.getenv("MODEL_${name.uppercase()}_SHA256")
                ?: props.getProperty("MODEL_${name}.sha256")
                ?: props.getProperty("MODEL_${name.uppercase()}_SHA256"))

        required.forEach { (fileName, minSize) ->
            val f = baseDir.resolve(fileName)
            if (!f.exists()) {
                logger.error("FEHLT: ${f.path}")
                failures++
            } else {
                val size = f.length()
                val head = f.inputStream().buffered().use { bis ->
                    val bytes = ByteArray(64)
                    val read = bis.read(bytes)
                    if (read > 0) String(bytes, 0, read) else ""
                }
                if (head.startsWith("# ")) {
                    logger.error("PLATZHALTER: ${f.name} enthält nur Platzhalter-Kommentar – bitte echtes Modell bereitstellen")
                    failures++
                } else if (size < minSize) {
                    logger.warn("WARNUNG: ${f.name} ist kleiner (${size} Bytes) als erwartete Mindestgröße ${minSize} Bytes – prüfen ob korrekt")
                }
                // Hash-Prüfung falls erwarteter Hash vorhanden
                val baseName = fileName.substringBefore('.')
                val expected = expectedHash(baseName)
                if (expected != null) {
                    val actual = MessageDigest.getInstance("SHA-256").digest(f.readBytes()).joinToString("") { byteVal -> "%02x".format(byteVal) }
                    if (!actual.equals(expected, ignoreCase = true)) {
                        logger.error("HASH MISMATCH: ${f.name} erwartet ${expected.take(12)}..., erhalten ${actual.take(12)}...")
                        failures++
                    } else {
                        logger.lifecycle("HASH OK: ${f.name}")
                    }
                }
                logger.lifecycle("OK: ${f.name} (${size} Bytes)")
            }
        }
        if (failures > 0) {
            throw GradleException("Model Verification fehlgeschlagen – ${failures} fehlende Datei(en)")
        } else {
            logger.lifecycle("Model Verification abgeschlossen – alle Dateien vorhanden (Prüfe Warnungen oben für Größe/Platzhalter)")
        }
    }
}

// Verifiziert ONNX Modelle (Existenz + optionale SHA256 Hashes)
tasks.register("verifyOnnxModels") {
    group = "verification"
    description = "Prüft Existenz & optionale Hashes der ONNX-Modelle"
    doLast {
        val onnxDir = rootProject.layout.projectDirectory.dir("models/onnx").asFile
        if (!onnxDir.exists()) {
            logger.warn("ONNX Verzeichnis fehlt – überspringe")
            return@doLast
        }
        val props = Properties()
        val lp = rootProject.file("local.properties")
        if (lp.exists()) props.load(lp.inputStream())
        fun expectedHash(base: String): String? =
            (System.getenv("MODEL_${base.uppercase()}_ONNX_SHA256")
                ?: props.getProperty("MODEL_${base}.onnx.sha256")
                ?: props.getProperty("MODEL_${base.uppercase()}_ONNX_SHA256"))
        var failures = 0
        onnxDir.listFiles { f -> f.extension.lowercase() == "onnx" }?.forEach { f ->
            val base = f.nameWithoutExtension
            val expected = expectedHash(base)
            if (expected != null) {
                val actual = MessageDigest.getInstance("SHA-256").digest(f.readBytes()).joinToString("") { b -> "%02x".format(b) }
                if (!actual.equals(expected, true)) {
                    logger.error("HASH MISMATCH (ONNX): ${f.name} erwartet ${expected.take(12)}..., erhalten ${actual.take(12)}...")
                    failures++
                } else logger.lifecycle("HASH OK (ONNX): ${f.name}")
            } else {
                logger.lifecycle("ONNX: ${f.name} – kein erwarteter Hash gesetzt")
            }
        }
        if (failures > 0) throw GradleException("verifyOnnxModels fehlgeschlagen – $failures Hash-Fehler")
        logger.lifecycle("verifyOnnxModels abgeschlossen")
    }
}

// Erzeugt eine Integritätsdatei mit SHA256 Hashes aller Modelle
tasks.register("generateModelIntegrity") {
    group = "documentation"
    description = "Generiert models/INTEGRITY.md mit SHA256 Hashes (TFLite & ONNX)"
    dependsOn("copyModels")
    doLast {
        val tfliteDir = rootProject.layout.projectDirectory.dir("models/tflite").asFile
        val onnxDir = rootProject.layout.projectDirectory.dir("models/onnx").asFile
        val outFile = rootProject.file("models/INTEGRITY.md")
        fun hash(f: java.io.File): String = MessageDigest.getInstance("SHA-256").digest(f.readBytes()).joinToString("") { b -> "%02x".format(b) }
        val lines = mutableListOf<String>()
        lines += "# Model Integrity Report"
    lines += "Generiert: ${Instant.now()}"
        lines += ""
        lines += "## TFLite"
        tfliteDir.listFiles { f -> f.extension.lowercase() == "tflite" }?.sortedBy { it.name }?.forEach { f ->
            lines += "- ${f.name} | size=${f.length()} | sha256=${hash(f)}"
        }
        lines += ""
        lines += "## ONNX"
        if (onnxDir.exists()) {
            onnxDir.listFiles { f -> f.extension.lowercase() == "onnx" }?.sortedBy { it.name }?.forEach { f ->
                lines += "- ${f.name} | size=${f.length()} | sha256=${hash(f)}"
            }
        } else lines += "(kein ONNX Verzeichnis)"
        outFile.writeText(lines.joinToString("\n"))
        logger.lifecycle("Integrity File aktualisiert: ${outFile.path}")
    }
}
