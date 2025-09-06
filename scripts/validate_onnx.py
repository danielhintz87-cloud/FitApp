import os, sys
import onnx

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
ONNX_DIR = os.path.join(ROOT, 'models', 'onnx')

if not os.path.isdir(ONNX_DIR):
    print(f"ONNX Verzeichnis fehlt: {ONNX_DIR}", file=sys.stderr)
    sys.exit(1)

failures = 0
for fn in sorted(os.listdir(ONNX_DIR)):
    if not fn.endswith('.onnx'): continue
    path = os.path.join(ONNX_DIR, fn)
    print(f"Prüfe {fn} ...")
    try:
        model = onnx.load(path)
        onnx.checker.check_model(model)
        g = model.graph
        inputs = [(i.name, [d.dim_value for d in i.type.tensor_type.shape.dim]) for i in g.input]
        outputs = [(o.name, [d.dim_value for d in o.type.tensor_type.shape.dim]) for o in g.output]
        print(f"  Inputs: {inputs}")
        print(f"  Outputs: {outputs}")
    except Exception as e:
        print(f"  FEHLER: {e}", file=sys.stderr)
        failures += 1

if failures:
    print(f"VALIDIERUNG FEHLGESCHLAGEN: {failures} Datei(en)", file=sys.stderr)
    sys.exit(1)
else:
    print("Alle ONNX Modelle gültig.")
