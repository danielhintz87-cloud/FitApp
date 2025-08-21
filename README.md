# FitApp

Basic skeleton for a flexible fitness and nutrition Android application built with Jetpack Compose.

## Modules
- `app` – Android application module using Compose with a top tab layout and placeholder screens for:
  - Setup
  - Workout
  - Kalorien
  - Ernährung
  - Einkauf
  - Fortschritt

## Build

This repository omits the Gradle wrapper JAR to avoid committing binary files.
Generate it with `gradle wrapper` or run with a locally installed Gradle.

To list available tasks run:

```bash
gradle tasks
```

Building the project requires the Android SDK. Configure `local.properties` with your SDK path before running:

```bash
gradle build
```
