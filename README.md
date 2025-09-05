# FitApp 🏋️‍♀️

[![Android CI](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android-room-ci.yml/badge.svg)](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android-room-ci.yml)
[![Android Tests](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android_tests.yml/badge.svg)](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android_tests.yml)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/gradle-8.14.3-blue.svg)](https://gradle.org)

Eine umfassende Android-Fitness-App mit KI-Integration, entwickelt mit Jetpack Compose und Material 3 Design.

## 🚀 Features

### Kernfunktionen
- **🏃‍♀️ Trainings-Tracking** - Vollständige Workout-Planung und -Verfolgung
- **🥗 Ernährungs-Management** - Kalorienzählung mit AI-Rezept-Vorschlägen
- **📊 Fortschritts-Analyse** - Detaillierte Charts und Statistiken
- **🎯 Achievement-System** - Persönliche Erfolge und Streak-Tracking
- **⚖️ Gewichts-Tracking** - BMI-Berechnung und Gewichtsverlauf
- **🤖 AI Personal Trainer** - Gemini & Perplexity AI Integration

### Technische Highlights
- **🎨 Material 3 Design** - Moderne und intuitive Benutzeroberfläche
- **🏗️ Clean Architecture** - MVVM mit Repository Pattern
- **💾 Room Database** - Lokale Datenspeicherung mit Migrationen
- **🔄 Reactive Programming** - Kotlin Coroutines und Flow
- **🧪 Automatisierte Tests** - Unit Tests und Instrumented Tests

## 🛠️ Entwicklung

### Anforderungen
- **Java 17** (erforderlich für Kompilierung)
- **Android SDK 34** (Minimum SDK 24)
- **Gradle 8.14.3** (via Wrapper)
- **2GB+ RAM** für Gradle Daemon

### Setup
```bash
# Repository klonen
git clone https://github.com/danielhintz87-cloud/FitApp.git
cd FitApp

# Abhängigkeiten installieren und bauen
cp local.properties.sample local.properties
# API-Schlüssel in local.properties hinzufügen (optional)

./gradlew clean assembleDebug
```

### Build-Befehle
```bash
# Debug Build (4-5 Minuten)
./gradlew assembleDebug

# Release Build (5-7 Minuten)  
./gradlew assembleRelease

# Tests ausführen
./gradlew testDebugUnitTest

# Lint-Prüfung
./gradlew lintDebug

# Vollständige Validierung
./gradlew check
```

## 🏗️ CI/CD Pipeline

### Automatisierte Workflows
- **✅ Build & Test**: Automatische Builds bei jedem Push/PR
- **✅ Room Schema Guard**: Datenbankschema-Validierung
- **✅ Instrumented Tests**: UI-Tests auf Android Emulatoren
- **✅ Code Quality**: Lint-Prüfung und Stilvalidierung
- **✅ Artifact Upload**: Test-Reports und Build-Ergebnisse

### Workflow-Status
| Workflow | Status | Beschreibung |
|----------|--------|--------------|
| Android CI | [![Android CI](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android-room-ci.yml/badge.svg)](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android-room-ci.yml) | Build + Schema Guard + Tests |
| Android Tests | [![Android Tests](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android_tests.yml/badge.svg)](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android_tests.yml) | Umfassende Test-Suite |

## 📁 Projektstruktur

```
app/src/main/java/com/example/fitapp/
├── ai/                 # AI-Integration (Gemini, Perplexity)
├── data/              # Repository Pattern, Room Database
│   ├── db/           # Datenbank-Entitäten, DAOs, Migrationen
│   └── prefs/        # SharedPreferences Wrapper
├── services/          # Business Logic Manager
├── ui/               # Compose Screens und Komponenten
└── utils/            # Hilfsfunktionen
```

## 🔧 Konfiguration

### API-Schlüssel
Erstelle `local.properties` mit:
```properties
GEMINI_API_KEY=dein_gemini_schlüssel
PERPLEXITY_API_KEY=dein_perplexity_schlüssel
```

### Gradle-Optimierung
- **Configuration Cache**: Aktiviert für schnellere Builds
- **Build Cache**: Wiederverwendung von Build-Outputs
- **Parallel Execution**: Parallele Abhängigkeitsauflösung

## 📊 Test-Coverage

- **Unit Tests**: Business Logic und Repository Tests
- **Instrumented Tests**: UI und Datenbank-Integrationstests
- **Schema Tests**: Room-Datenbankmigrationen

## 🤝 Beitragen

1. Fork das Repository
2. Erstelle einen Feature-Branch (`git checkout -b feature/amazing-feature`)
3. Commit deine Änderungen (`git commit -m 'Add amazing feature'`)
4. Push zum Branch (`git push origin feature/amazing-feature`)
5. Erstelle einen Pull Request

### Entwicklungsrichtlinien
- Folge bestehenden Kotlin-Code-Stil
- Verwende Material 3 Komponenten
- Schreibe Tests für neue Features
- Aktualisiere Documentation bei API-Änderungen

## 📄 Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert.

## 🚀 Deployment

Die App wird automatisch durch GitHub Actions gebaut und getestet. Release-Builds werden bei Git-Tags automatisch erstellt.

---

**Entwickelt mit ❤️ für Fitness-Enthusiasten**