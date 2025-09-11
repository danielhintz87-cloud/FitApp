# Copilot Instructions for FitApp

Diese Datei definiert, wie GitHub Copilot (inkl. Copilot Coding Agent) in diesem Repository arbeiten soll: Ziele, Architekturleitlinien, Qualitätskriterien, Sicherheitsvorgaben, Branch-/PR-Konventionen, Tests und Migrationsregeln.

## 1) Repository-Kontext und Ziele
- Plattform: Android (Kotlin)
- Kern-Stacks:
  - UI: Jetpack Compose
  - DI: Hilt
  - Persistenz: Room (mit sauberer Migrationskette)
  - Einstellungen: DataStore (Proto)
  - Concurrency: Kotlin Coroutines (Dispatcher-Disziplin, kein Netzwerk auf Main)
  - Kamera/ML: CameraX, ML Kit (geplant/teils vorhanden), spätere echte Inferenz
  - Health: Health Connect (Herzfrequenz, Sync)
  - AI-Provider: z. B. Gemini, Perplexity (IO-bound, robustes Fehlerhandling)
- Qualitätsziele:
  - Stabiler Build, saubere Migrationskette (keine destruktiven Deletes)
  - Threading robust und testbar (Dispatcher injizierbar)
  - Persistente, testbare Einstellungen via DataStore Proto
  - Defensive Fehlerbehandlung und Telemetrie-Hooks
  - Gute Testabdeckung für kritische Pfade (Migrations, IO/Dispatcher, DataStore, HealthChecker)

Siehe Roadmap/Backlog (u. a. Collections-Persistenz, SyncQueue-Persistenz, Barcode-DB, Meal Planner, Timer, ML, Video, A11y). Der PR #275 adressiert die Stabilisierung (Phase 1).

## 2) Arbeitsweise des Copilot Coding Agent
Wenn du (Copilot) eine Pull Request erstellst:
- Branching:
  - feat/`<feature-name>` oder fix/`<bug-name>` oder chore/`<task-name>`
  - Kleine, fokussierte PRs (nicht mehr als 3-5 Dateien, außer bei Refactoring)
  - Beschreibender Name mit Problem-Statement und Kriterien

- Problem Statement Format:
  ```
  **Goal**: [Was wird erreicht?]
  **Scope**: [Was ist im Scope/Out-of-Scope?]
  **Acceptance Criteria**: [Checkliste der Erfolgskriterien]
  ```

- Bilder-Policy: 
  - Falls Screenshots/Bilder hinzugefügt werden: in `/docs/images/` ablegen
  - Relative Pfade verwenden: `![Alt Text](../docs/images/screenshot.png)`
  - Bilder komprimiert (< 500KB) und beschreibende Alt-Texte

- Tests schreiben:
  - Für neue Features: Unit Tests + ggf. Instrumentation Tests
  - Für kritische Pfade (Migration, Dispatcher, DataStore): obligatorisch
  - Coroutine-Tests mit `runTest` und `StandardTestDispatcher`

- Commits:
  - Semantische Messages: `feat: add XYZ`, `fix: resolve ABC`, `chore: update DEF`
  - Kleine, atomare Commits bevorzugt
  - Build muss nach jedem Commit erfolgreich sein

## 3) Architektur & Code-Leitlinien

### 3.1 Threading & Dispatchers
- **Niemals** `Dispatchers.IO/Default/Main` direkt verwenden
- Stattdessen `withContext(dispatchers.io)` für IO-bound Operationen verwenden
- Injection via `DispatcherProvider` Interface (z. B. `DefaultDispatcherProvider` mit `Dispatchers.IO/Main/Default`).
- Tests: `StandardTestDispatcher` und gezielte Thread-Asserts verwenden.

### 3.2 Room & Migrationen
- Keine destruktiven Migrations (keine "fallbackToDestructiveMigration").
- Additive Migrationen (ALTER TABLE, CREATE + COPY), Daten erhalten.
- Jede Schema-Version muss abgedeckt sein; Migrationstests sind Pflicht.
- Schema-Export pflegen (Gradle `room.schemaLocation`) und in Tests nutzen.

### 3.3 DataStore (Proto)
- user_prefs.proto: Feldnummern sind stabil; keine Renumberings nach Merge.
- Entfernte Felder via `reserved` schützen.
- Defaults eindeutig definieren und dokumentieren.
- Migrations von SharedPreferences schrittweise; verlustfrei; in separaten PRs finalisieren.

### 3.4 Health Checker
- Einheitliche Schnittstelle (z. B. `HealthCheckable` -> `HealthStatus`).
- Registry, um Checker dynamisch zu registrieren/abzufragen.
- Fehlerpfade (Timeouts, HTTP-Fehler, Exceptions) standardisiert behandeln.
- UI kann Status „PENDING/OK/ERROR" anzeigen.

### 3.5 Fehlerbehandlung & Logging
- Klare Exceptions/Result-Typen, keine „schluckenden" Catches.
- Log-Level sinnvoll: DEBUG (Entwicklung), WARN/ERROR (Produktion).
- Kein Loggen sensibler Daten/Schlüssel.

### 3.6 Sicherheit & Secrets
- Keine Secrets im Code/Repo (API Keys, Tokens).
- Nutzung von sicheren Quellen (CI-Secrets, lokale Properties).
- BuildConfig-Flags nur für nicht-sensible Feature-Flags.

### 3.7 UI/UX & A11y
- Compose: StateFlows/immutable States nutzen, keine UI-Blocking-Calls.
- A11y: Semantics, Fokussteuerung, Screenreader, Kontraste beachten.
- Skeleton/Loading: Animiert, aber a11y-freundlich (versteckt, wo sinnvoll).

### 3.8 Performance
- Vermeide unnötige Recomposition/Allocation.
- Caching-Strategien für Medien/Modelle (späterer Ausbau).
- Off-main Work strikt enforced.

## 4) Build & Test

### 4.1 Build
- JDK 17 (empfohlen, falls Projekt so konfiguriert).
- Gradle:
  - `./gradlew clean assembleDebug`
  - Wichtige Plugins: kapt/ksp (Room), protobuf (DataStore Proto)
- Lint/Static Checks (falls konfiguriert): `./gradlew lint detekt ktlintCheck` (anpassen, falls vorhanden)

### 4.2 Tests
- Unit: `./gradlew testDebugUnitTest`
- Instrumentation: `./gradlew connectedDebugAndroidTest`
- Migrationstests: Room Test DB mit exportierten Schemas, Upgrade alt -> neu.
- Coroutine-Tests: `runTest` + `StandardTestDispatcher`.

## 5) PR-Checkliste (Selbstprüfung)
- [ ] Build grün lokal/CI
- [ ] Keine NetworkOnMainThreadException möglich (Dispatcher erzwungen)
- [ ] Room: Migrationskette vollständig + Migrationstest(e)
- [ ] DataStore: Defaults + Flow-API getestet; Proto-IDs stabil
- [ ] Fehlerhandling: klare Pfade, keine sensiblen Logs
- [ ] Tests vorhanden/aktualisiert (kritische Pfade)
- [ ] PR-Text: Ziele, Kriterien, Out-of-Scope; Bilder korrekt referenziert

## 6) Branch-/Release-Policy
- Ziel-Branch: `main`
- Kleine, thematisch fokussierte PRs bevorzugt.
- Semantische Commit Messages empfohlen (feat/fix/chore/docs/test/build).

## 7) Was Copilot NICHT tun soll
- Keine destruktiven DB-Migrationen.
- Keine Secrets/Schlüssel ins Repo schreiben.
- Keine langen Laufzeiten in UI-Threads.
- Keine Proto-Feldnummern nach Merge ändern.
- Keine ungetesteten Kernpfad-Änderungen.

## 8) Nächste Wellen (Kurzüberblick)
- P1: Collections-Persistenz, SyncQueue-Persistenz, HealthChecker Ausbau, DataStore Migration finalisieren
- P2: Barcode DB + ML Kit Integration, Meal Planner, Timer (Coroutine-Countdown)
- P3: ML-Inferenz echt (TFLite/MediaPipe), Video-Caching/Adaptive Streaming, Coach Trigger, A11y-Verbesserungen

---
Hinweis: Wenn du (Copilot) einen PR aufmachst, halte dich strikt an die oben beschriebenen Problem-Statement-Regeln, Testpflichten und Migrations-/Dispatcher-Standards.