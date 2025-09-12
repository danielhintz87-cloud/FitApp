# PR-Überprüfung für Modularisierungskompatibilität

Diese Checkliste hilft bei der Überprüfung und Anpassung bestehender Pull Requests, um Kompatibilität mit der neuen Modulstruktur zu gewährleisten.

## Allgemeine Kompatibilitätsprüfung

- [ ] Verwendung von `DispatcherProvider` statt direkter Dispatchers
- [ ] Einhaltung der Thread-Disziplin (kein Netzwerk auf Main-Thread)
- [ ] Einheitliche Fehlerbehandlung mit `Result`-Klasse
- [ ] Korrekte Hilt-Annotation und -Einbindung
- [ ] Keine destruktiven Datenbankmigrationen
- [ ] Stabile Proto-IDs in DataStore

## Modulstruktur-Anpassungen

### Navigation

- [ ] Navigation über `NavigationRegistry` statt direkter NavController-Nutzung
- [ ] Routen als Konstanten in `NavigationRoutes` definiert
- [ ] NavigationProvider für Feature-spezifische Routen implementiert

### UI-Komponenten

- [ ] Gemeinsame UI-Komponenten in `core:ui` verwendet
- [ ] Theme-Definitionen über `FitAppTheme` genutzt
- [ ] MainScaffold durch modulare Version ersetzt

### Datenbanken & DataStore

- [ ] Room-DAOs und Entitäten in richtigen Modulen platziert
- [ ] Migrationsstrategie korrekt implementiert
- [ ] DataStore-Zugriff über Repository abstrahiert

### Dependency Injection

- [ ] Hilt-Module korrekt auf Module verteilt
- [ ] Interfaces in `core:domain`, Implementierungen in `core:data` oder Feature-Modulen
- [ ] Korrekte Scope-Annotation (Singleton, ActivityScoped, etc.)

## Spezifische PR-Anpassungen

### #323: Integration von Mockk

✅ Bereits gelöst: mockk-Abhängigkeit in `libs.versions.toml` hinzugefügt.

### #322: PerplexityAiProvider Fix

- [ ] `PerplexityAiProvider` in passendes Modul (z.B. `core:data` oder feature-spezifisch) verschieben
- [ ] Korrekte Dispatcher-Nutzung sicherstellen

### #321: NutritionRepository Hilt-Integration

- [ ] Interface in `core:domain` definieren
- [ ] Implementierung in Feature-Modul oder `core:data` platzieren
- [ ] Hilt-Binding über passendes Modul bereitstellen

### #320: MainScaffold Navigation Modularisierung

- [ ] Ersetzen durch neue modulare Navigation
- [ ] Feature-Navigation über `NavigationProvider` implementieren
- [ ] Bottom-Navigation vereinfachen und in `core:ui` integrieren

### Übrige PRs

Ähnliches Vorgehen für weitere PRs:

1. Code-Änderungen analysieren
2. Zielmodule identifizieren
3. Änderungen entsprechend der Modulstruktur anpassen
4. Tests aktualisieren
5. Dokumentation ergänzen

## Vorgehen bei Konflikten

1. Identifizieren des betroffenen Codes
2. Entscheiden, in welches Modul der Code gehört
3. Anpassen der Importe und Abhängigkeiten
4. Bei größeren Umstrukturierungen: PR in kleinere, sequentielle PRs aufteilen

## Konsolidierungsphase

Nach Anpassung aller PRs:

- Vollständige Testsuite durchführen
- Build-Performance prüfen
- Dokumentation aktualisieren
- Architekturdiagramm erstellen