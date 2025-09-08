#!/usr/bin/env bash
set -euo pipefail

REPORT_XML="app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
OUT_DIR="badges"
BADGE_FILE="${OUT_DIR}/coverage.json"

if [ ! -f "$REPORT_XML" ]; then
  echo "Jacoco report not found: $REPORT_XML" >&2
  exit 0
fi

lineNode=$(grep -o '<counter type="LINE"[^>]*/>' "$REPORT_XML" | head -n1 || true)
if [ -z "$lineNode" ]; then
  echo "No LINE counter found" >&2
  exit 0
fi
missed=$(echo "$lineNode" | sed -n 's/.*missed="\([0-9]\+\)".*/\1/p')
covered=$(echo "$lineNode" | sed -n 's/.*covered="\([0-9]\+\)".*/\1/p')
if [ -z "$missed" ] || [ -z "$covered" ]; then
  echo "Could not parse coverage" >&2
  exit 0
fi

total=$((missed + covered))
if [ "$total" -eq 0 ]; then
  pct=0
else
  pct=$(awk -v c="$covered" -v t="$total" 'BEGIN { printf "%.1f", (c/t)*100 }')
fi

color=red
if (( $(echo "$pct >= 80" | bc -l) )); then color=green; elif (( $(echo "$pct >= 60" | bc -l) )); then color=yellow; fi

mkdir -p "$OUT_DIR"
cat > "$BADGE_FILE" <<EOF
{"schemaVersion":1,"label":"coverage","message":"${pct}%","color":"${color}"}
EOF

echo "Coverage badge written to $BADGE_FILE ( ${pct}% )"
