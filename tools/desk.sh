#!/usr/bin/env bash
# Compiles the platform-independent core plus the desktop review tools and runs one of them.
# Usage: tools/desk.sh <MainClass> [args...]
set -euo pipefail
cd "$(dirname "$0")/.."
export JAVA_TOOL_OPTIONS=""
OUT=build/desk
rm -rf "$OUT" && mkdir -p "$OUT"
javac -encoding UTF-8 -nowarn -d "$OUT" $(find app/src/main/java/de/chr1ss0/ssp/core desktop/src -name '*.java') 2>&1 | grep -v "Picked up" || true
test -f "$OUT/de/chr1ss0/ssp/desktop/$1.class"
CLS="$1"; shift
java -cp "$OUT" "de.chr1ss0.ssp.desktop.$CLS" "$@" 2>&1 | grep -v "Picked up"
