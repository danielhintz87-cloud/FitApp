import os
import sys

# Installation:
#   python3 -m venv .venv && source .venv/bin/activate
#   pip install --upgrade pip
#   pip install tflite2onnx onnx

def convert_tflite_to_onnx(src_tflite: str, dst_onnx: str):
    from tflite2onnx.converter import convert
    os.makedirs(os.path.dirname(dst_onnx), exist_ok=True)
    print(f"Converting {src_tflite} -> {dst_onnx}")
    convert(src_tflite, dst_onnx, experimental_opset=True)

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TFLITE_DIR = os.path.join(ROOT, "models", "tflite")
ONNX_DIR = os.path.join(ROOT, "models", "onnx")

pairs = [
    (os.path.join(TFLITE_DIR, "movenet_thunder.tflite"),
     os.path.join(ONNX_DIR, "movenet_thunder.onnx")),
    (os.path.join(TFLITE_DIR, "blazepose.tflite"),
     os.path.join(ONNX_DIR, "blazepose.onnx")),
]

missing = [src for src, _ in pairs if not os.path.exists(src)]
if missing:
    print("Fehlende TFLite-Dateien:\n - " + "\n - ".join(missing), file=sys.stderr)
    print("Bitte zuerst 'bash scripts/fetch_models.sh' ausführen.", file=sys.stderr)
    sys.exit(1)

for src, dst in pairs:
    convert_tflite_to_onnx(src, dst)

print("ONNX-Modelle erstellt in:", ONNX_DIR)