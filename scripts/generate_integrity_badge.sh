#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INTEGRITY_FILE="$ROOT_DIR/models/INTEGRITY.md"
BADGE_DIR="$ROOT_DIR/badges"
mkdir -p "$BADGE_DIR"
if [ ! -f "$INTEGRITY_FILE" ]; then
  echo '{"schemaVersion":1,"label":"models","message":"no-report","color":"lightgrey"}' > "$BADGE_DIR/models_integrity.json"
  exit 0
fi
# Zähle TFLite & ONNX
TFLITE_COUNT=$(grep -E '^-' "$INTEGRITY_FILE" | grep '.tflite' | wc -l | tr -d ' ')
ONNX_COUNT=$(grep -E '^-' "$INTEGRITY_FILE" | grep '.onnx' | wc -l | tr -d ' ')
TOTAL=$((TFLITE_COUNT + ONNX_COUNT))
COLOR=green
if [ "$ONNX_COUNT" -eq 0 ]; then COLOR=yellow; fi
if [ "$TFLITE_COUNT" -eq 0 ]; then COLOR=red; fi
jq -n --arg t "$TFLITE_COUNT" --arg o "$ONNX_COUNT" --arg tot "$TOTAL" --arg c "$COLOR" '{schemaVersion:1,label:"models",message:($tot+" total / "+$t+" tflite / "+$o+" onnx"),color:$c}' > "$BADGE_DIR/models_integrity.json"
echo "Generated $BADGE_DIR/models_integrity.json"
