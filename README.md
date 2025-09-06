# FitApp 🏋️‍♀️

[![Android CI](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android-room-ci.yml/badge.svg)](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android-room-ci.yml)
[![Android Tests](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android_tests.yml/badge.svg)](https://github.com/danielhintz87-cloud/FitApp/actions/workflows/android_tests.yml)
[![Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/danielhintz87-cloud/FitApp/main/badges/coverage.json)](./badges/coverage.json)
[![Models](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/danielhintz87-cloud/FitApp/main/badges/models_integrity.json)](./badges/models_integrity.json)
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

### Coverage Badge
Das Coverage-Badge wird automatisch über den Workflow `update-badges.yml` aktualisiert. Die Rohdaten liegen unter `badges/coverage.json`.

## 🤖 ML Modelle
### Integritätsprüfung (SHA-256)
ONNX Hashes (optional) analog – aktuell gepflegt (Lightning generiert, Thunder optional):
```
export MODEL_MOVENET_LIGHTNING_ONNX_SHA256=435bd2411997e60030d4731bd3f33a3e21fc9d1f9aac39245cb31f301be3b14a
export MODEL_MOVENET_THUNDER_ONNX_SHA256=<sha256>   # (falls später konvertiert)
export MODEL_BLAZEPOSE_ONNX_SHA256=<sha256>         # (Task-Format, Konvertierung derzeit übersprungen)
```

### Integritätsreport generieren
```bash
./gradlew :app:generateModelIntegrity
cat models/INTEGRITY.md
```
Optional können erwartete Hashes gesetzt werden, damit `:app:verifyModels` / `:app:verifyOnnxModels` Integrität erzwingt:
```bash
export MODEL_MOVENET_THUNDER_SHA256=<sha256>
export MODEL_BLAZEPOSE_SHA256=<sha256>
# optional
export MODEL_MOVEMENT_ANALYSIS_MODEL_SHA256=<sha256>
```
Oder in `local.properties`:
Beispiel (aktuelle Repository-Versionen):
```
# MoveNet Thunder
MODEL_MOVENET_THUNDER.sha256=41641538679ec79b07d4101e591dda47d098c09af29607674b2a40b8a3798dd3
# BlazePose
MODEL_BLAZEPOSE.sha256=5134a3aad27a58b93da0088d431f366da362b44e3ccfbe3462b3827a839011b1
# Movement Analysis (MoveNet Lightning Variante)
MODEL_MOVEMENT_ANALYSIS_MODEL.sha256=0fac2226112d0371903ca86e3853cec24ef603a0b2f96f589b180f0ebdd135ab
MODEL_MOVENET_LIGHTNING_ONNX.sha256=435bd2411997e60030d4731bd3f33a3e21fc9d1f9aac39245cb31f301be3b14a
```
```
MODEL_MOVENET_THUNDER.sha256=<sha256>
MODEL_BLAZEPOSE.sha256=<sha256>
```


Die App nutzt mehrere On-Device Modelle:

| Typ | Datei | Quelle |
|-----|-------|--------|
| Pose (MoveNet Thunder) | `models/tflite/movenet_thunder.tflite` | TF Hub |
| Pose (BlazePose Landmark Full) | `models/tflite/blazepose.tflite` | MediaPipe |
| Movement Analysis (MoveNet Lightning) | `models/tflite/movement_analysis_model.tflite` | TF Hub (Lightning, repurposed) |

ONNX-Konvertierungen (MoveNet Lightning aktuell) via SavedModel Pipeline:
```bash
# Erst TFLite Modelle herunterladen (falls noch nicht)
bash scripts/fetch_models.sh

# Optional Variante (lightning | thunder)
export MOVENET_VARIANT=lightning

# SavedModel Export + ONNX Konvertierung
python scripts/export_savedmodel_and_convert.py

# (Legacy Fallback) Versuch für reine TFLite→ONNX
python scripts/convert_to_onnx.py || true
```

Verifikation & Integritätsupdate:
```bash
./gradlew :app:verifyOnnxModels :app:generateModelIntegrity
cat models/INTEGRITY.md
```

### Modelle beziehen / aktualisieren
```bash
# Optional echte Movement-Analyse-URL und Checksum bereitstellen
export MOVEMENT_ANALYSIS_MODEL_URL="https://example.com/path/movement_analysis_model.tflite"
export MOVEMENT_ANALYSIS_MODEL_SHA256="<sha256>"

# Modelle herunterladen
bash scripts/fetch_models.sh

# (Re-)Verifikation
./gradlew :app:verifyModels
```

Alternativ kann in `local.properties` gesetzt werden:
```
MOVEMENT_ANALYSIS_MODEL_URL=https://example.com/path/movement_analysis_model.tflite
```

### CI Secrets
In GitHub Actions Secrets hinterlegen:
- `MOVEMENT_ANALYSIS_MODEL_URL`
- (optional) `MOVEMENT_ANALYSIS_MODEL_SHA256`

Automatische Prüfung erfolgt im Code Quality Workflow (`Verify ML Models`).

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