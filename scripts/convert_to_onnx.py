import os
import sys
from typing import List

# Installation:
#   python3 -m venv .venv && source .venv/bin/activate
#   pip install --upgrade pip
#   pip install tflite2onnx onnx

def convert_tflite_to_onnx(src_tflite: str, dst_onnx: str):
    # tflite2onnx exposes convert function in convert.convert
    from tflite2onnx.convert import convert
    os.makedirs(os.path.dirname(dst_onnx), exist_ok=True)
    print(f"Converting {src_tflite} -> {dst_onnx}")
    convert(src_tflite, dst_onnx)

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TFLITE_DIR = os.path.join(ROOT, "models", "tflite")
ONNX_DIR = os.path.join(ROOT, "models", "onnx")

if not os.path.isdir(TFLITE_DIR):
    print(f"TFLite Verzeichnis fehlt: {TFLITE_DIR}", file=sys.stderr)
    sys.exit(1)

# Dynamisch alle .tflite konvertieren – außer sehr kleine Platzhalter (< 1KB) oder spezielle Dateien
tflite_files: List[str] = [
    os.path.join(TFLITE_DIR, f) for f in sorted(os.listdir(TFLITE_DIR)) if f.lower().endswith('.tflite')
]

if not tflite_files:
    print("Keine TFLite Modelle gefunden – bitte fetch_models.sh ausführen", file=sys.stderr)
    sys.exit(1)

skipped: List[str] = []
converted: List[str] = []
failed: List[str] = []

for src in tflite_files:
    size = os.path.getsize(src)
    base = os.path.splitext(os.path.basename(src))[0]
    if base.startswith("blazepose"):
        skipped.append(f"{os.path.basename(src)} (MediaPipe Task/landmark format – Konvertierung übersprungen)")
        continue
    if size < 1024:  # Platzhalter oder Mini-Datei überspringen
        skipped.append(f"{os.path.basename(src)} (zu klein: {size} Bytes – vermutlich Platzhalter)")
        continue
    dst = os.path.join(ONNX_DIR, f"{base}.onnx")
    try:
        convert_tflite_to_onnx(src, dst)
        converted.append(os.path.basename(dst))
    except Exception as e:
        err = f"{os.path.basename(src)} -> {os.path.basename(dst)}: {e}"
        failed.append(err)
        print(f"FEHLER (fortgesetzt) {err}", file=sys.stderr)

print("\n=== Zusammenfassung ===")
if converted:
    print("Konvertiert:")
    for c in converted:
        print(f" - {c}")
else:
    print("Keine Modelle konvertiert")
if skipped:
    print("Übersprungen:")
    for s in skipped:
        print(f" - {s}")
if failed:
    print("Fehlgeschlagen:")
    for f in failed:
        print(f" - {f}")
print("ONNX-Modelle Zielverzeichnis:", ONNX_DIR)

# Exit-Code Logik: nur Fehlercode, wenn kein einziges Modell konvertiert wurde und mindestens eins fehlgeschlagen ist
if not converted and failed:
    sys.exit(2)