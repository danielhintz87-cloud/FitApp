# Scripts zum Laden und Konvertieren der Modelle

## 1) Modelle herunterladen
```bash
bash scripts/fetch_models.sh
```
Lädt:
- models/tflite/movenet_thunder.tflite (TF Hub, float16)
- models/tflite/blazepose.tflite (MediaPipe BlazePose Landmark Full)

Zusätzlich enthalten:
- models/tflite/movement_analysis_model.tflite (Bewegungsanalysemodell, aus UCI-HAR-Daten trainiert)

## 2) (Optional) TFLite -> ONNX konvertieren
```bash
python3 -m venv .venv && source .venv/bin/activate
pip install --upgrade pip
pip install tflite2onnx onnx
python scripts/convert_to_onnx.py
```
Erzeugt:
- models/onnx/movenet_thunder.onnx
- models/onnx/blazepose.onnx

## 3) Modelle in App-Assets kopieren (automatisch vor dem Build)
```bash
./gradlew :app:copyModels
# oder einfach:
./gradlew assembleDebug
```