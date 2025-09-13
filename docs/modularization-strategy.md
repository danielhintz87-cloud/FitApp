# Modularisierungsstrategie für FitApp

Dieses Dokument beschreibt die Strategie zur schrittweisen Modularisierung der FitApp.

## Ausgangssituation

Die FitApp ist derzeit als monolithische App strukturiert:

- Alle Features sind direkt im app-Modul implementiert
- MainScaffold.kt ist stark mit allen Screens gekoppelt (833 Zeilen)
- Fehlende Abstraktion zwischen UI, Domain und Daten
- Duplikation von Code und fehlende klare Verantwortlichkeiten

## Zielarchitektur

Die neue Architektur basiert auf folgenden Prinzipien:

1. **Klare Trennung von Verantwortlichkeiten**:
   - Core-Module für gemeinsame Funktionalität
   - Feature-Module für isolierte Anwendungsfunktionen

2. **Robuste Navigation**:
   - Zentrales Navigationssystem mit Registry
   - Entkoppelte Feature-Navigation

3. **Konsistente Patterns**:
   - Einheitliche Dispatcher-Nutzung
   - Standardisierte Fehlerbehandlung
   - Konsistente Repository-Struktur

## Phasen der Modularisierung

### Phase 1: Grundlegende Infrastruktur ✅

- Core-Module erstellen (domain, data, ui, navigation)
- Gemeinsame Gradle-Konfiguration
- Navigation-Registry implementieren
- Basiskomponenten wie DispatcherProvider, Result-Klasse

### Phase 2: MainScaffold umstellen

- MainScaffold zu ModularNavHost migrieren
- BottomNavigation als gemeinsamen UI-Komponente auslagern
- Registrierungsmechanismus für Features einrichten

### Phase 3: Feature-Extraktion

1. **Kernfunktionalität in Hauptmodulen konsolidieren**:
   - Health-Connect-Integration in core:health
   - SharedPreferences/DataStore-Migration in core:data
   - Room-Datenbank-Logik in core:data
   - Dispatchers in core:domain
   - UI-Komponenten in core:ui

2. **Features isolieren**:
   - Hydration-Feature extrahieren
   - Tracking-Feature extrahieren
   - Nutrition-Feature extrahieren
   - Workout-Feature extrahieren

### Phase 4: Testing und Stabilisierung

- Unit-Tests für jedes Modul ergänzen
- Instrumentation-Tests aktualisieren
- Sicherstellen, dass alle Features unabhängig funktionieren
- Dokumentation aktualisieren

## Migration-Checkliste für jedes Feature

1. **Vorbereitung**:
   - Analysieren der Feature-Grenzen
   - Identifizieren von gemeinsam genutzten Komponenten

2. **Struktur erstellen**:
   - Feature-Modul anlegen
   - Abhängigkeiten konfigurieren
   - NavigationProvider implementieren

3. **Code migrieren**:
   - Screens und ViewModels extrahieren
   - Repository-Interfaces in core:domain, Implementierungen in feature
   - Datenbank-Zugriff über core:data abstrahieren

4. **Testen**:
   - Funktionalität überprüfen
   - Tests aktualisieren oder neu schreiben

## Entscheidungen

### Module oder Packages?

Wir haben uns für eine echte Modularisierung mit Gradle-Modulen entschieden, statt nur Package-Strukturen zu verwenden, aus folgenden Gründen:

- Stärkere Entkopplung und Abhängigkeitskontrolle
- Möglichkeit zur separaten Kompilierung und schnelleren Builds
- Bessere Trennung von Verantwortlichkeiten
- Vorbereitung auf Dynamic Feature Modules in der Zukunft

### Warum nicht Feature-First?

Wir haben uns gegen eine reine Feature-First-Architektur entschieden, bei der jedes Feature seine eigene komplette Schichtung hat, und stattdessen für eine Mischung aus Feature- und Layer-Modularisierung entschieden:

- Core-Module für gemeinsame Funktionalität reduzieren Duplikation
- Feature-Module für isolierte Anwendungsfunktionen
- Bessere Balance zwischen Isolation und Code-Sharing

## Nächste Schritte

1. Navigation-Registry in app-Modul einbinden
2. MainScaffold refaktorieren
3. Health-Connect-Integration in core:health konsolidieren
4. Hydration-Feature als erstes Feature-Modul extrahieren