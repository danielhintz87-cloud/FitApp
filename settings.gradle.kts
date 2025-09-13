pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.12.1"
        id("com.android.library") version "8.12.1"
        id("org.jetbrains.kotlin.android") version "2.0.20"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
        id("com.google.devtools.ksp") version "2.0.20-1.0.25"
        id("com.google.dagger.hilt.android") version "2.48"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    }
}


plugins {
    // Falls schon vorhanden, lass die Version so stehen, wichtig ist der develocity-Block unten
    id("com.gradle.develocity") version "3.19.2"
}

develocity {
    buildScan {
        // 👉 Bedingungen automatisch akzeptieren, damit keine CLI-Abfrage kommt
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"

        // 👉 Lokale Builds NICHT automatisch veröffentlichen
        publishing.onlyIf { false }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitApp"

// App und Wear OS Module
include(":app")
include(":wear")

// Core Module
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:navigation")

// Feature Module (Beispiele, können später aktiviert werden)
include(":feature:hydration")
include(":feature:tracking")