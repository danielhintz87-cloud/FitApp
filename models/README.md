# Modelle

Dieses Verzeichnis enthält die Pose-Modelle (TFLite) und – optional – konvertierte ONNX-Versionen.

## Quellen
- MoveNet Thunder (Single Pose, float16, TFLite)
  - TF Hub: https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/float16/4
  - Download via Script: `scripts/fetch_models.sh`
- BlazePose Landmark (Full, TFLite)
  - MediaPipe: `mediapipe/modules/pose_landmark/pose_landmark_full.tflite`
  - Download via Script: `scripts/fetch_models.sh`

## Dateien
- TFLite:
- `models/tflite/movenet_thunder.tflite`
- `models/tflite/blazepose.tflite`
  - `models/tflite/movement_analysis_model.tflite` (Bewegungsanalysemodell, trainiert auf UCI-HAR)
- ONNX (optional, via `scripts/convert_to_onnx.py` oder CI-Workflow `onnx-convert.yml`):
  - `models/onnx/movenet_thunder.onnx`
  - `models/onnx/blazepose.onnx`

## Lizenz-Hinweise
Bitte die jeweiligen Google-/MediaPipe-Lizenzen beachten.

## Versionierung
Große Binärdateien nicht direkt committen; ggf. Git LFS verwenden (siehe `.gitattributes`).

## Automatisierte Konvertierung
Der Workflow `.github/workflows/onnx-convert.yml` konvertiert TFLite → ONNX bei Änderungen und committet aktualisierte Dateien (falls Unterschiede). Manuell auslösbar über "Run workflow".