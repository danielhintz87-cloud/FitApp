#!/usr/bin/env python3
import os, sys, tempfile, shutil, subprocess, json
import tensorflow as tf

# This script exports a TF Hub MoveNet model (lightning or thunder) to a SavedModel
# and converts it to ONNX using tf2onnx. It reuses existing downloaded tflite file only
# as reference but fetches TF Hub model via tfhub.

import urllib.request

MOVENET_VARIANTS = {
    "lightning": "https://tfhub.dev/google/movenet/singlepose/lightning/4",
    "thunder": "https://tfhub.dev/google/movenet/singlepose/thunder/4"
}

variant = os.environ.get("MOVENET_VARIANT", "lightning")
if variant not in MOVENET_VARIANTS:
    print(f"Unsupported variant {variant}. Choose one of {list(MOVENET_VARIANTS)}", file=sys.stderr)
    sys.exit(2)

try:
    import tensorflow_hub as hub
except ImportError:
    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'tensorflow_hub'])
    import tensorflow_hub as hub

root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
onnx_dir = os.path.join(root, 'models', 'onnx')
os.makedirs(onnx_dir, exist_ok=True)

print(f"Loading TF Hub model for MoveNet {variant}...")
model = hub.load(MOVENET_VARIANTS[variant])

# Build concrete function
input_signature = tf.TensorSpec([1, 192, 192, 3], dtype=tf.int32, name='input')
serving_fn = model.signatures['serving_default']
# Wrap to enforce input spec (forward as 'input')
@tf.function(input_signature=[input_signature])
def wrapped(tensor):
    return serving_fn(input=tensor)

saved_model_dir = tempfile.mkdtemp(prefix='movenet_savedmodel_')
print(f"Exporting SavedModel to {saved_model_dir}")
tf.saved_model.save(model, saved_model_dir, signatures={'serving_default': wrapped})

# Convert to ONNX
output_onnx = os.path.join(onnx_dir, f'movenet_{variant}.onnx')
print(f"Converting to ONNX: {output_onnx}")
subprocess.check_call([
    sys.executable, '-m', 'tf2onnx.convert',
    '--saved-model', saved_model_dir,
    '--opset', '17',
    '--output', output_onnx
])

print("ONNX model created:", output_onnx)

# Cleanup temp
shutil.rmtree(saved_model_dir, ignore_errors=True)
