#!/usr/bin/env bash
set -euo pipefail

REPO="danielhintz87-cloud/FitApp"

require_gh() {
  if ! command -v gh >/dev/null 2>&1; then
    echo "GitHub CLI (gh) ist nicht installiert. Siehe https://cli.github.com/" >&2
    exit 1
  fi
  if ! gh auth status -h github.com >/dev/null 2>&1; then
    echo "Bitte melde dich an: gh auth login" >&2
    exit 1
  fi
}

create_label() {
  local name="$1"
  local color="$2"
  local description="$3"
  gh label create "$name" --color "$color" --description "$description" -R "$REPO" --force >/dev/null 2>&1 || true
}

create_issue() {
  local title="$1"
  local labels="$2"
  local body_content="$3"

  local tmpfile
  tmpfile="$(mktemp)"
  printf "%s" "$body_content" > "$tmpfile"
  gh issue create -R "$REPO" --title "$title" --label "$labels" --body-file "$tmpfile"
  rm -f "$tmpfile"
}

main() {
  require_gh

  echo "Erstelle/aktualisiere Labels im Repository $REPO ..."
  create_label "enhancement" "a2eeef" "Neue Funktion oder Verbesserung"
  create_label "UI/UX" "00bcd4" "Design und Nutzererlebnis"
  create_label "high-priority" "d73a4a" "Hohe Priorität"
  create_label "architecture" "5319e7" "Architektur & Patterns"
  create_label "refactoring" "fbca04" "Code-Refactoring"
  create_label "database" "1d76db" "Datenbank & Persistenz"
  create_label "cloud-sync" "0e8a16" "Cloud Synchronisation"
  create_label "feature" "0e8a16" "Neues Feature"
  create_label "AI/ML" "5319e7" "Künstliche Intelligenz / Machine Learning"
  create_label "innovation" "bfd4f2" "Innovative Erweiterungen"
  create_label "nutrition" "006d32" "Ernährung"
  create_label "wearables" "c2e0c6" "Wearable-Integration"
  create_label "health-monitoring" "f9d0c4" "Gesundheits-Überwachung"
  create_label "gamification" "c5def5" "Gamification"
  create_label "social" "0366d6" "Soziale Features"
  create_label "security" "b60205" "Sicherheit"
  create_label "privacy" "d4c5f9" "Datenschutz"
  create_label "compliance" "e4e669" "Compliance/DSGVO"
  create_label "testing" "a2eeef" "Tests"
  create_label "CI/CD" "0052cc" "Build & Deployment"
  create_label "quality-assurance" "e4e669" "Qualitätssicherung"

  echo "Erstelle Issues ..."

  create_issue "🎨 Komplettes UI/UX Redesign mit Material Design 3" "enhancement,UI/UX,high-priority" "$(cat <<'EOF'
## Beschreibung
Die aktuelle Benutzeroberfläche der FitApp verwendet veraltete Design-Patterns und bietet keine moderne, intuitive User Experience. Ein komplettes Redesign mit Material Design 3 (Material You) bringt die App auf den neuesten Stand und verbessert die Usability deutlich.

## Ziele
- Material Design 3 mit dynamischen Farbthemen (Material You)
- Klare, schnelle Navigation (Bottom Navigation + FAB)
- Motion-Design: sanfte Übergänge, Micro-Animationen
- Responsive Layouts für Phone/Tablet/Foldables
- Dark Mode, hohe Kontraste, Barrierefreiheit

## Aufgaben
- [ ] Migration auf Material 3 Komponenten (M3 Theme, Material3 Widgets)
- [ ] Theme-System mit Dynamic Colors (Android 12+), Fallback-Palette
- [ ] Redesign aller Layouts (Cards, Chips, TopAppBar, BottomBar)
- [ ] State-basierte Loading-UI (Skeletons/Shimmers)
- [ ] Einführen von Motion-Transitions (MaterialSharedAxis, FadeThrough)
- [ ] Überarbeitung der Iconographie (Material Symbols Rounded)
- [ ] Typografie-Scale und Spacing-System vereinheitlichen
- [ ] Accessibility: TalkBack, Touch-Targets, Contrast, ContentDescription
- [ ] Design-Dokumentation (Figma-Link, UI-Kit, Komponenten-Guidelines)

## Technische Hinweise
- com.google.android.material:material (Material3)
- DynamicColors.applyToActivitiesIfAvailable()
- Navigation: AndroidX Navigation + BottomNav + Single Activity

## Akzeptanzkriterien
- Konsistentes M3-Theme in allen Screens
- WCAG AA Kontrast erfüllt
- Dark Mode vollständig unterstützt
- Navigationsfluss in ≤2 Taps zu Kernfunktionen
EOF
)"

  create_issue "🏗️ Architektur-Refactoring: MVVM + Clean Architecture" "architecture,refactoring,high-priority" "$(cat <<'EOF'
## Beschreibung
Aktuell liegt Business- und UI-Logik stark in Activities/Fragments. Das erschwert Wartung, Tests und Erweiterbarkeit. Ziel ist MVVM mit Clean Architecture (Domain, Data, Presentation) sowie DI.

## Ziele
- Strikte Layer-Trennung (Domain/Data/Presentation)
- ViewModels + UseCases + Repository Pattern
- Coroutines/Flow für reaktive Datenströme
- Dependency Injection mit Hilt
- Höhere Testbarkeit (Unit- und Integrationstests)

## Aufgaben
- [ ] Modul-/Paketstruktur nach Clean Architecture aufsetzen
- [ ] UseCases für zentrale Businesslogik extrahieren
- [ ] Repository-Interfaces und Implementierungen erstellen
- [ ] ViewModels mit StateFlow/UiState einführen
- [ ] DI mit Hilt (Module, Scopes, AssistedInject falls nötig)
- [ ] ViewBinding/Compose-Interoperabilität klären
- [ ] Unit-Tests für UseCases/ViewModels
- [ ] Tech-Doku: Architekturleitfaden, Diagramme

## Akzeptanzkriterien
- Keine Businesslogik mehr in Activities/Fragments
- 70%+ Testabdeckung in Domain-Layer
- Unidirektionaler Datenfluss, klare Verantwortlichkeiten
EOF
)"

  create_issue "💾 Datenbank-Migration auf Room + Cloud Sync" "database,enhancement,cloud-sync" "$(cat <<'EOF'
## Beschreibung
Die aktuelle SQLite-Nutzung ist veraltet. Migration zu Room mit Migrations, TypeConverters und optionaler Cloud-Synchronisation (z. B. Firebase/Firestore) schafft Stabilität und Multi-Device-Fähigkeit.

## Ziele
- Room ORM inkl. Migrations und TypeConverters
- Offline-First, Konflikt-Strategien, Background Sync
- Sichere Speicherung (optional SQLCipher)
- Export/Import und Backups

## Aufgaben
- [ ] Entities/DAOs/Database erstellen, Migrations definieren
- [ ] Suspend/Flow-DAOs, Transaktionssicherheit
- [ ] Sync-Service (Pending Queue, Retry, Konfliktlösung)
- [ ] Verschlüsselung sensibler Daten (SQLCipher/Jetpack Security)
- [ ] Daten-Export (JSON verschlüsselt) und Import
- [ ] Performance: Indizes, Paging, Projections
- [ ] Tests: DAO-, Migration- und Sync-Tests

## Akzeptanzkriterien
- Zero-Downtime Migration (bestehende Daten bleiben erhalten)
- Datenkonsistenz trotz Offline/Online-Wechsel
- 10x schnellere, indizierte Kernabfragen
EOF
)"

  create_issue "🏋️ KI-gestütztes Personal Training (On-Device)" "feature,AI/ML,innovation" "$(cat <<'EOF'
## Beschreibung
Ein ML-gestütztes Trainingssystem erstellt personalisierte Pläne, analysiert Fortschritt und gibt Form-Feedback in Echtzeit. Fokus auf On-Device (TFLite) für Privatsphäre und Latenz.

## Ziele
- Personalisierte, adaptive Trainingspläne
- Pose-/Formanalyse (Kamera optional)
- Progressive Overload, Deload-Phasen
- Verletzungsprävention

## Aufgaben
- [ ] TFLite-Integration, Modellverwaltung (AB-Tests)
- [ ] Pose Detection (ML Kit/PoseNet), Keypoint-Features
- [ ] Form-Scoring, Echtzeit-Hinweise (Audio/Haptik)
- [ ] Plan-Generator mit Feedback-Schleife (Leistung/Recovery)
- [ ] Datenschutz: On-Device Inferenz, Opt-in Kamera
- [ ] Telemetrie anonymisiert (Modellgüte, Latenzen)
- [ ] Unit-/Instrumented-Tests (Determinismus, Edge-Cases)

## Akzeptanzkriterien
- Reproduzierbare Planergebnisse bei gleichen Inputs
- Latenz Formanalyse ≤ 100ms auf Mid-Range Devices
- Opt-in/Opt-out Kamera, klare Datenschutzhinweise
EOF
)"

  create_issue "🥗 Intelligentes Ernährungstracking (Barcode + Bilderkennung)" "feature,nutrition,AI/ML" "$(cat <<'EOF'
## Beschreibung
Ernährungserfassung per Barcode-Scan und KI-basierter Bilderkennung für Mahlzeiten inkl. Portionsgrößenschätzung und automatischer Nährwertberechnung.

## Ziele
- Schnelle Erfassung (Foto/Barcode)
- OpenFoodFacts-Integration
- Portions-/Makro-Berechnung
- Personalisierte Empfehlungen

## Aufgaben
- [ ] ML Kit Barcode-Scanner (kontinuierlich, Low-Light optimiert)
- [ ] Bilderkennung (Custom Model + Heuristiken), Segmentierung
- [ ] Portionsermittlung (Referenzobjekte, Plattengröße, Tiefe)
- [ ] OpenFoodFacts API + Caching
- [ ] Nährstoff-Dashboard, Tagesziele, Wasser-Reminder
- [ ] Rezeptvorschläge & Einkaufslisten
- [ ] Datenschutz: lokale Verarbeitung wenn möglich

## Akzeptanzkriterien
- ≥95% Barcode-Erkennungsrate bei gängigen Produkten
- Schlüsselflüsse ≤3 Aktionen (öffnen → scannen → speichern)
- Abgleich und Korrektur-UI bei Mehrdeutigkeiten
EOF
)"

  create_issue "⌚ Wearable-Integration & Echtzeit-Gesundheitsmonitoring" "feature,wearables,health-monitoring" "$(cat <<'EOF'
## Beschreibung
Integration mit Google Fit/Health Connect/Wear OS für Echtzeit-Herzfrequenz, automatische Trainingserkennung, Schlaf- und Recovery-Analysen.

## Ziele
- Health Connect als zentrale Drehscheibe
- Echtzeitdaten (HF/HRV/Schritte/Sessions)
- Automatische Aktivitätserkennung
- Recovery-Score

## Aufgaben
- [ ] Health Connect Permissions/Abfragen, Daten-Mapping
- [ ] Wear OS Companion (Start/Pause/Stop, HF-Anzeige)
- [ ] Realtime-Streams (CallbackFlow), Zonenberechnung
- [ ] Schlafphasen- und HRV-Analysen, Recovery-Score
- [ ] Alerts (Übertraining, hohe HF, Dehydrierung)
- [ ] Energiespar-Strategien (Foreground Service, Batch)

## Akzeptanzkriterien
- Stabile Datenströme ohne Memory Leaks
- Recovery-Empfehlungen nachvollziehbar begründet
- Nutzer kann Datenteilung fein granular steuern
EOF
)"

  create_issue "🎮 Gamification & Social Features" "feature,gamification,social" "$(cat <<'EOF'
## Beschreibung
Motivation durch Erfolge, XP, Streaks, Challenges und einen Social Feed. Optionale Community-Wettkämpfe und Leaderboards.

## Ziele
- Achievement-System (mit Raritäten)
- XP-/Level-Progression
- Tägliche/Wöchentliche Challenges
- Social Feed & Leaderboards

## Aufgaben
- [ ] Achievement-Engine (Events, Regeln, Belohnungen)
- [ ] Streak-Tracking mit Verzeihfenster
- [ ] PvP/Community-Challenges mit Matchmaking
- [ ] Feed-Karten (Workouts, PRs, Achievements)
- [ ] Leaderboards (global, Freunde, lokal) + Caching
- [ ] Missbrauchsschutz & Moderation (Meldungen, Blocken)

## Akzeptanzkriterien
- Belohnungen/Progress klar kommuniziert (Toasts/Banner)
- Anti-Cheating-Heuristiken (Anomalien, Rate Limits)
- DSGVO: soziale Freigaben sind Opt-in, granular
EOF
)"

  create_issue "🔒 Sicherheit, Datenschutz & DSGVO-Compliance" "security,privacy,compliance,high-priority" "$(cat <<'EOF'
## Beschreibung
Maximale Sicherheit für Gesundheitsdaten: Verschlüsselung, biometrische Authentifizierung, DSGVO-konforme Einwilligungen, Export/Löschung, Audit-Logging.

## Ziele
- Ende-zu-Ende Schutz sensibler Daten
- Biometrik & sichere Schlüsselverwaltung
- Vollständige DSGVO-Workflows
- Audits & Härtung

## Aufgaben
- [ ] Datenbankverschlüsselung (SQLCipher) + Key in Android Keystore
- [ ] BiometricPrompt (Fallback PIN/Passwort), Session-Lock
- [ ] Consent Manager (Opt-in/out, Versionierung, Historie)
- [ ] Datenexport (verschlüsselt), Right to be Forgotten (Soft->Hard Delete)
- [ ] Certificate Pinning, TLS Hardened, Security Headers
- [ ] Crash/Logs ohne PII, Pseudonymisierung
- [ ] Penetration Testing, Threat Modeling (STRIDE)

## Akzeptanzkriterien
- Vollständige Consent-Historie je Nutzer
- Export/Deletion in < 24h durchführbar (manuell/automatisiert)
- Regelmäßige Security Review-Checkliste vorhanden
EOF
)"

  create_issue "🧪 Umfassende Test-Suite & CI/CD Pipeline" "testing,CI/CD,quality-assurance" "$(cat <<'EOF'
## Beschreibung
Aufbau einer breiten Test-Suite (Unit/Integration/UI/Performance) und CI/CD mit automatisierten Checks, Artefakten und optionalem Play-Store-Track.

## Ziele
- ≥80% Coverage im Domain-Layer
- Stabile UI-Tests der Kernflüsse
- Automatisierte Lint/Static Analysis
- Schnelle, reproduzierbare Builds

## Aufgaben
- [ ] JUnit5/MockK Setup, Test Fixtures, MainDispatcherRule
- [ ] DAO-/Repository-Tests (Test DB, Testcontainers bei Bedarf)
- [ ] Espresso/Compose UI-Tests kritischer Flows
- [ ] Lint/Detekt/ktlint + JaCoCo Coverage Report
- [ ] GitHub Actions: Test-, Lint-, Build-Jobs, Artefakte
- [ ] Benchmark/Startup-Tests (Macrobenchmark)
- [ ] Crashlytics & ANR Monitoring (Release-Builds)

## Akzeptanzkriterien
- Rote Pipeline blockt Merge in main
- Testberichte & Coverage als Artefakte verfügbar
- UI-Tests stabil (Flake-Rate < 2%)
EOF
)"

  echo "Fertig! Die Issues wurden im Repository $REPO erstellt."
}

main "$@"