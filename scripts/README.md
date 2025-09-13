# Scripts Verzeichnis

## Modelle-Scripts

### 1) Modelle herunterladen
```bash
bash scripts/fetch_models.sh
```
Lädt:
- models/tflite/movenet_thunder.tflite (TF Hub, float16)
- models/tflite/blazepose.tflite (MediaPipe BlazePose Landmark Full)

Zusätzlich enthalten:
- models/tflite/movement_analysis_model.tflite (Bewegungsanalysemodell, aus UCI-HAR-Daten trainiert)

### 2) (Optional) TFLite -> ONNX konvertieren
```bash
python3 -m venv .venv && source .venv/bin/activate
pip install --upgrade pip
pip install tflite2onnx onnx
python scripts/convert_to_onnx.py
```
Erzeugt:
- models/onnx/movenet_thunder.onnx
- models/onnx/blazepose.onnx

### 3) Modelle in App-Assets kopieren (automatisch vor dem Build)
```bash
./gradlew :app:copyModels
# oder einfach:
./gradlew assembleDebug
```

## GitHub Issues Creation Script

### create_issues.sh

Ein Bash-Script, das automatisch 9 umfassende GitHub Issues für die FitApp erstellt, inklusive:

1. 🎨 **UI/UX Redesign mit Material Design 3** - Komplette Überarbeitung der Benutzeroberfläche
2. 🏗️ **Architektur-Refactoring** - MVVM + Clean Architecture Implementation  
3. 💾 **Datenbank-Migration** - Room ORM + Cloud Sync
4. 🏋️ **KI-gestütztes Personal Training** - On-Device ML für personalisierte Trainings
5. 🥗 **Intelligentes Ernährungstracking** - Barcode + Bilderkennung
6. ⌚ **Wearable-Integration** - Health Connect + Echtzeit-Monitoring
7. 🎮 **Gamification & Social Features** - Achievements, Leaderboards, Community
8. 🔒 **Sicherheit & DSGVO-Compliance** - Ende-zu-Ende Verschlüsselung, Datenschutz
9. 🧪 **Test-Suite & CI/CD** - Umfassende Qualitätssicherung

#### Voraussetzungen

- [GitHub CLI (gh)](https://cli.github.com/) installiert
- Authentifizierung mit GitHub: `gh auth login`
- Schreibberechtigung für das Repository `danielhintz87-cloud/FitApp`

#### Verwendung

```bash
# Script ausführbar machen
chmod +x scripts/create_issues.sh

# Issues erstellen
./scripts/create_issues.sh
```

Das Script erstellt automatisch:
- Alle benötigten Labels mit passenden Farben
- 9 detaillierte Issues mit Beschreibungen, Aufgaben und Akzeptanzkriterien
- Passende Label-Zuweisungen für jedes Issue

#### Labels

Das Script erstellt folgende Labels automatisch:
- `enhancement`, `UI/UX`, `high-priority`
- `architecture`, `refactoring`, `database`
- `cloud-sync`, `feature`, `AI/ML`
- `innovation`, `nutrition`, `wearables`
- `health-monitoring`, `gamification`, `social`
- `security`, `privacy`, `compliance`
- `testing`, `CI/CD`, `quality-assurance`

#### Issue-Struktur

Jedes Issue folgt einem konsistenten Format:
- **Beschreibung**: Problemstellung und Kontext
- **Ziele**: Klare Zielsetzungen
- **Aufgaben**: Detaillierte Checkliste mit konkreten Schritten
- **Akzeptanzkriterien**: Messbare Erfolgskriterien