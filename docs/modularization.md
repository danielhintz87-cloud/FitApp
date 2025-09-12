# FitApp Modularisierung

Diese Dokumentation beschreibt die neue Modulstruktur der FitApp und gibt Leitlinien für die Erstellung neuer Features.

## Modul-Struktur

Die FitApp verwendet eine modulare Architektur mit folgenden Hauptkategorien:

### Core Module

- **core:domain**: Enthält Geschäftslogik, Use Cases, Modelle und Interfaces für Repositories
  - DispatcherProvider für konsistente Thread-Verwaltung
  - Result-Klassen für einheitliche Fehlerbehandlung
  - Keine Android-spezifischen Abhängigkeiten (soweit möglich)

- **core:data**: Implementiert die Datenzugriffs- und Persistenzschicht
  - Room-Datenbank und DAOs
  - Datastore für Einstellungen
  - Repository-Implementierungen
  - API-Clients und Netzwerklogik

- **core:ui**: Enthält gemeinsame UI-Komponenten und Themes
  - MaterialTheme-Definitionen
  - Wiederverwendbare Compose-Komponenten
  - Grundlegende UI-Utilities

- **core:navigation**: Stellt das Navigationssystem bereit
  - NavigationRegistry für modulare Navigation
  - NavigationProviders für Feature-Module
  - Gemeinsame Navigationsdestinationen

### Feature Module

Feature-Module sind in sich geschlossene Anwendungsfunktionen, die über die NavigationRegistry in die App integriert werden:

- **feature:hydration**: Wasserzähler-Feature
- **feature:tracking**: Trainings-Tracking-Feature

Jedes Feature-Modul sollte seine eigene UI, Domain und Datenlogik enthalten, aber die gemeinsamen Kernkomponenten wiederverwenden.

## Dependency Injection

Die FitApp verwendet Hilt für Dependency Injection:

- Jedes Modul definiert seine eigenen Module für die benötigten Abhängigkeiten
- Core-Module stellen Basisabhängigkeiten wie DispatcherProvider bereit
- Feature-Module können ihre NavigationProvider in die Registry eintragen

## Navigation

Die Navigation wurde modularisiert:

1. **NavigationRegistry**: Zentrale Registry für alle Navigationsziele
2. **NavigationProvider**: Interface für Feature-Module, um ihre Navigationsziele zu registrieren
3. **NavigationDestination**: Basisklasse für alle Navigationsziele
4. **ComposeNavigationDestination**: Spezialisierte Klasse für Compose-Screens

Beispiel für die Registrierung eines Feature-Moduls:

```kotlin
@Singleton
class HydrationNavigationProvider @Inject constructor() : NavigationProvider {
    override fun registerDestinations(registry: NavigationRegistry) {
        registry.registerDestination(
            composeDestination(NavigationRoutes.Hydration.MAIN)
                .setContent { navController, _ ->
                    HydrationScreen(navController)
                }
                .build()
        )
    }
}
```

## Richtlinien für neue Features

1. **Modulstruktur**: Neue Features als eigene Module anlegen
2. **Abhängigkeiten**: Nur von Core-Modulen abhängig sein, nicht von anderen Features
3. **Navigation**: NavigationProvider implementieren und registrieren
4. **Threading**: Immer DispatcherProvider verwenden, nie direkte Dispatchers
5. **Fehlerbehandlung**: Result-Klasse für einheitliche Fehlerbehandlung nutzen
6. **Testing**: Unit-Tests für kritische Logik, besonders für Migrations und Dispatchers

## Gradle-Konfiguration

Für neue Module die gemeinsame Konfiguration verwenden:

```kotlin
// Beispiel für ein Feature-Modul
configureAndroidLibrary(
    namespace = "de.hhn.fitapp.feature.myfeature",
    dependencies = ModuleDependencies.FEATURE_BASIC,
    enableCompose = true
)

// Spezifische Abhängigkeiten
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
}
```

## Migration

Die Migration zu dieser Architektur erfolgt schrittweise:

1. Gemeinsame Core-Module erstellen ✅
2. MainScaffold auf ModularNavHost umstellen
3. Feature für Feature in eigene Module extrahieren
4. Tests anpassen und sicherstellen, dass alles funktioniert