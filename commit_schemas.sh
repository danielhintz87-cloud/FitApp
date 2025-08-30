#!/bin/bash
# Commit Room schemas automatically to the repository

set -e

cd "$(dirname "$0")/app"

if [ ! -d "schemas" ]; then
  echo "❌ Kein app/schemas-Ordner gefunden. Bitte einmal bauen!"
  exit 1
fi

cd ..

git add app/schemas
git commit -m "Update Room schemas [auto]"
git push

echo "✅ Schemas wurden committed und gepusht."