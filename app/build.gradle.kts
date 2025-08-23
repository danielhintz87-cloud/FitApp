import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // OPENAI_API_KEY aus Gradle-Property ODER local.properties einlesen
        val gradleKey = providers.gradleProperty("OPENAI_API_KEY").orNull
        val localKey = run {
            val f = rootProject.file("local.properties")
            if (f.exists()) Properties().apply { load(f.inputStream()) }.getProperty("OPENAI_API_KEY")
            else null
        }
        val key = gradleKey ?: localKey ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$key\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    android.buildFeatures.buildConfig = true

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/README*"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Pager (HorizontalPager ist im foundation-Paket enthalten)
    implementation("androidx.compose.foundation:foundation")

    // Bilder (optional für spätere Food-Fotos)
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.google.android.material:material:1.12.0")

    // OpenAI Java SDK
    implementation("com.openai:openai-java:2.3.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
