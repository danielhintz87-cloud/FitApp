pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.12.2"
        id("org.jetbrains.kotlin.android") version "1.9.24"
        id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
        id("org.jetbrains.kotlin.kapt") version "1.9.24"
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
include(":app")