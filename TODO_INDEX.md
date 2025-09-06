# Konsolidierter TODO Index

Automatisch konsolidiert (Stand: 2025-09-06)

## Kategorien
- UI & UX
- Daten & Persistenz
- Health / Integrationen
- Audio & Medien
- Infrastruktur / Cleanup
- Feature-Erweiterungen

## UI & UX
- Barcode Scanner einbauen (`FoodSearchScreen.kt:63`) – Vorschlag: ML Kit Barcode Scanning API oder ZXing Embedding. 
- Voice Input Einkaufszettel (`SmartShoppingListScreen.kt:137`) – Vorschlag: SpeechRecognizer API Wrapper + Coroutine CallbackFlow.
- Help/Support Screen (`MainScaffold.kt:288`) – Eigenen Navigation Route `help` + statischer Markdown Renderer.
- About Screen (`MainScaffold.kt:298`) – App Info (VersionName, BuildType), Open Source Lizenzen.
- Privacy Policy öffnen (`HealthConnectSettingsScreen.kt:334`) – Externe URL oder In-App WebView.
- Help Button (`HealthConnectSettingsScreen.kt:380`) – Kontextsensitives FAQ.

## Daten & Persistenz
- Migration SharedPreferences → DataStore (`UserPreferences.kt:23,67`) – Create Proto schema `user_prefs.proto`, Migrations definieren.
- Workout Tracking Kopplung der verbrannten Kalorien (`FoodDiaryScreen.kt:116`) – Koppelung an WorkoutExecutionManager / MET Werte.
- User Kontext für Workout Execution (`WorkoutExecutionManager.kt:100`) – UserSession Provider Interface.
- Track Actual Rest Time & Duration (`WorkoutExecutionManager.kt:218,221`) – Zeitstempel Start/Ende + Differenz.
- Water Tracking korrekte Integration (`WaterReminderWorker.kt:21`) – NutritionRepository hydration Tabelle/Entity.
- Reset Manager nicht implementierte DAO Calls (`ResetManager.kt:486,490,852,853`) – Prüfen ob Entities existieren; falls nicht: Entities definieren.

## Health / Integrationen
- Health Connect Permissions anfragen (`HealthConnectSettingsScreen.kt:71`) – Implementiere Permission Launcher für required DataTypes.
- Last Sync Time aus Preferences laden (`HealthConnectSettingsScreen.kt:42`).

## Audio & Medien
- Audio Playback für SmartRestTimer (`SmartRestTimer.kt:223`) – ExoPlayer oder SoundPool für kurze Clips.

## Infrastruktur / Cleanup
- Privacy Policy & Help URLs konfigurieren (evtl. in `local.properties`).
- Hash Baseline für neue DataStore Migration Tests anlegen.

## Feature-Erweiterungen / ML
- Barcode Scanner könnte Nährstoffe auto-ausfüllen (Mapping Tabelle).
- Voice Input Intent-Erkennung (Produktkategorien normalisieren).

## Priorisierung (Vorschlag)
1. DataStore Migration (Basis für weitere Persistenz-Features)
2. Health Connect Permissions & Last Sync (User Value kurzfristig hoch)
3. Workout Execution: echte Zeiten (Analytics Qualität)
4. Barcode Scanner (UX Gewinn)
5. Audio Rest Timer (Motivation / Feedback)
6. Voice Input & Help/About Screens
7. ResetManager vollständige Bereinigung (Edge Cases)

## Technische Quick Wins
- Einführung `UserSessionProvider` Interface
- `HydrationEntryEntity` + DAO + Repository Methoden
- `RestTimerAudioPlayer` kleine Abstraktion für Sound
- `DataStoreModule` + Migrations Test

## Nächste Schritte (wenn beauftragt)
- Scaffold Branch: `feat/datastore-migration`
- Implement Proto + Repository Adapter
- Tests: Migration + Preferences Mapping

