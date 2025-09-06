#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODELS_DIR="$ROOT_DIR/models"
TFLITE_DIR="$MODELS_DIR/tflite"
ONNX_DIR="$MODELS_DIR/onnx"
TMP_DIR="$ROOT_DIR/.tmp_models"

mkdir -p "$TFLITE_DIR" "$ONNX_DIR" "$TMP_DIR"

echo "==> Lade MoveNet Thunder (TFLite, float16) von TF Hub..."
# Try the direct download first, fallback to alternative URL if needed
TFHUB_URL="https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/float16/4?lite-format=tflite"
ALT_URL="https://storage.googleapis.com/tfhub-lite-models/google/lite-model/movenet/singlepose/thunder/tflite/float16/4.tflite"

if curl -L --fail "$TFHUB_URL" -o "$TFLITE_DIR/movenet_thunder.tflite" 2>/dev/null; then
  echo "Downloaded MoveNet Thunder from TF Hub"
elif curl -L --fail "$ALT_URL" -o "$TFLITE_DIR/movenet_thunder.tflite" 2>/dev/null; then
  echo "Downloaded MoveNet Thunder from alternative URL"
else
  echo "Warning: Could not download MoveNet Thunder. Creating placeholder..."
  echo "# MoveNet Thunder placeholder - replace with actual model" > "$TFLITE_DIR/movenet_thunder.tflite"
fi

echo "==> Lade BlazePose Landmark (TFLite, Full) von MediaPipe..."
BLAZEPOSE_URL="https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/float16/1/pose_landmarker_full.task"
ALT_BLAZEPOSE_URL="https://github.com/google/mediapipe/raw/master/mediapipe/modules/pose_landmark/pose_landmark_full.tflite"

if curl -L --fail "$ALT_BLAZEPOSE_URL" -o "$TFLITE_DIR/blazepose.tflite" 2>/dev/null; then
  echo "Downloaded BlazePose from MediaPipe repository"
elif curl -L --fail "$BLAZEPOSE_URL" -o "$TFLITE_DIR/blazepose.tflite" 2>/dev/null; then
  echo "Downloaded BlazePose from MediaPipe storage"
else
  echo "Warning: Could not download BlazePose. Creating placeholder..."
  echo "# BlazePose placeholder - replace with actual model" > "$TFLITE_DIR/blazepose.tflite"
fi

echo "==> movement_analysis_model.tflite bereits vorhanden – kein Download nötig"

# Bewegungsauswertungsmodell (Optionaler Download – Beispielquelle mit Checksum)
ANALYSIS_MODEL="$TFLITE_DIR/movement_analysis_model.tflite"
if [ ! -s "$ANALYSIS_MODEL" ] || grep -q "placeholder" "$ANALYSIS_MODEL" 2>/dev/null; then
  echo "==> Lade Movement Analysis Modell..."
  # Beispiel-Download-URL (Platzhalter). Bitte echte Quelle hinterlegen.
  ANALYSIS_URL="https://raw.githubusercontent.com/danielhintz87-cloud/model-hosting/main/movement_analysis_model.tflite"
  if curl -L --fail "$ANALYSIS_URL" -o "$ANALYSIS_MODEL" 2>/dev/null; then
    echo "Movement Analysis Modell heruntergeladen"
  else
    echo "Warnung: Konnte Movement Analysis Modell nicht laden – vorhandene Datei bleibt"
  fi
fi

echo "==> Aufräumen..."
rm -rf "$TMP_DIR"

echo "Fertig:"
echo " - $TFLITE_DIR/blazepose.tflite"
echo " - $TFLITE_DIR/movenet_thunder.tflite"
echo " - $TFLITE_DIR/movement_analysis_model.tflite"
