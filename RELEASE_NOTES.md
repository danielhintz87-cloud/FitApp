# Release Candidate (RC1)

Ziel: Stabilisierung aller Blocker für ersten öffentlichen/erweiterten internen Test.

## Version
RC1 (intern) – Basis: PR #264

## Wichtige Änderungen (Blocker-Fixes)
### Migration & Daten
- Hinzugefügt: `MIGRATION_14_15` (No-Op) zur Schließung der Migrationskette
- Korrektur: `RecipeEntity` Initialisierung mit vollständigen Pflichtfeldern / sicheren Defaults
- Konsolidierung: Null-sichere Behandlung von IDs (z.B. `mapNotNull` in Food Diary Flow)

### AI & Netzwerk
- Netzwerkaufrufe (blocking) jetzt im IO-Dispatcher (keine `NetworkOnMainThreadException`)
- Vereinheitlichtes Fehler-/Statusmodell (AiUiState) im Personal Trainer & Generation
- Sichtbares Feedback bei: ungültigem Key, fehlender Konfiguration, Netzwerk-/RateLimit-Problemen

### UI & Navigation
- Rezeptgenerierung aus Ernährungshub erreichbar (Quick Action / Button)
- HIIT Builder & Execution Flow über Today-/Training-Hub aufrufbar
- Personal Trainer Buttons & Schnellaktionen funktional (Workout-Generierung, Analyse etc.)
- Verbesserte Fehlermeldungen (Snackbars / Inline Panels)

### Training & Streaks
- Ernährungs-Logging triggert Streak- & Achievement-Mechanik

### Wassertracking
- Einheitlicher Zielwert: aus `DailyGoalEntity`, Fallback 2000 ml
- Reminder, Diary & Analytics nutzen dieselbe Quelle

### ML / Pose Detection
- Einheitliche Generics: `MLResult<Pose?>`
- Degraded Fallback sauber markiert

## Stabilität / Qualität
- `assembleDebug` + `assembleRelease` erfolgreich (lokal)
- Keine Main-Thread-Netzwerkzugriffe in AI-Pfaden
- Verbesserte Logging-Struktur für Migration & AI-Fehler

## Manuelle Smoke-Test Matrix (Kurzfassung)
| Bereich | Test | Erwartung |
|--------|------|-----------|
| Migration | Start mit v14 DB | Kein Crash, Log "Migration 14→15 OK" |
| Ernährung | Rezept generieren | Erfolgreich, Favorit speichern |
| HIIT | Builder → Execution | Timer & Interval Flow |
| AI Trainer | Ungültiger Key | Warnhinweis |
| AI Trainer | Gültiger Key | Workout Ergebnis |
| Streaks | Mehrere Mahlzeiten | Streak steigt |
| Wasser | Ziel geändert | Anzeige konsistent |
| ML | Pose Detection | Kein Crash |

## Bekannte Einschränkungen
- Kein Offline-Cache für AI Antworten
- Kein differenziertes RateLimit-Retry
- Limited E2E Automation für HIIT & AI Flows

## Nächste Schritte Richtung Final Release
1. Erweiterter Beta-Test
2. Fehlerverdichtung & Telemetrie
3. Performance-Metriken AI
4. Compose UI Smoke Tests erweitern
5. Onboarding/Coach Intro prüfen

## Mitwirkende
Automatisierte Fixes + Integration durch Copilot (PR #264) + Projektbasis.

---
_Aktualisieren vor finalem Tag/Version._