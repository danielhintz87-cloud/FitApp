plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
    // Statische Analyse
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    // Modern versioning from Git tags
    id("pl.allegro.tech.build.axion-release") version "1.18.12"
}

// Modern versioning configuration using Git tags and semantic versioning
scmVersion {
    tag {
        prefix = "v"
        versionSeparator = ""
    }
    versionCreator.set { version, position ->
        val baseVersion = version.ifEmpty { "1.8.0" }
        if (position.isClean) {
            baseVersion
        } else {
            "$baseVersion-${position.shortRevision}"
        }
    }
}

// Make version available to subprojects
project.version = scmVersion.version

