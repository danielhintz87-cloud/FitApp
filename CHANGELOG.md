# Changelog

Alle wichtigen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.0.0/),
und dieses Projekt folgt [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

### Added
- Moderne Versionierung mit Git-Tags und automatischer Version-Generierung
- Zentrale Versionsverwaltung für App und Wear-Module
- Semantic Versioning Support mit axion-release Plugin
- Callback-basierte Navigation (Entkopplung von NavController in Screens: Plan, Today, Nutrition, TodayTraining, Settings-Unterseiten)
- Einheitliche Padding-Extension `applyContentPadding` für konsistentes Layout
- AI Fehlerklassifikation (`AiErrors.kt`) mit normalisierten Kategorien (auth, quota, rate_limit, server, etc.)
- Vereinheitlichte Logging-Tags für Gemini & Perplexity Provider

### Changed
- Versionierung erfolgt jetzt automatisch aus Git-Tags
- Build-Skripte nutzen zentrale Version aus Root-Projekt
- Gemini Provider bereinigt (entfernte doppelte Code-Pfade, konsolidierte Textgenerierung)
- Navigation in `MainScaffold` auf Callback-Routen aktualisiert
- Konsistente Fehler-Wrapping mit `ClassifiedAiException` in AI Providern

### Technical
- Axion-Release Plugin für Git-Tag-basierte Versionierung hinzugefügt
- Version-Code wird automatisch aus Major.Minor.Patch generiert
- Konsistente Versionierung zwischen App und Wear-Modulen
- Gemeinsame Modelloptimierungs- und Fehlerklassifikations-Infrastruktur vereinheitlicht
- Reduktion doppelter Padding-Ketten in zentralen Screens

## [1.8.0] - 2025-01-09

### Added
- Baseline für moderne Versionierung
- Aktueller Stand des Projekts mit allen Features

### Technical
- Initialer Release-Tag für Versionierung-Baseline
- Bestehende Features: Material 3 Design, Room Database, Health Connect, ML Models, Wear OS Support

---

## Versioning Schema

- **MAJOR**: Inkompatible API-Änderungen
- **MINOR**: Neue Features (rückwärts kompatibel)
- **PATCH**: Bugfixes (rückwärts kompatibel)

## Tagging Convention

- Tags folgen dem Format: `v<MAJOR>.<MINOR>.<PATCH>`
- Beispiel: `v1.8.0`, `v1.8.1`, `v1.9.0`, `v2.0.0`