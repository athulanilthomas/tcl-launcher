#!/usr/bin/env bash
set -euo pipefail

# Read-only ADB inventory. No TV settings or package state are modified.
command -v adb >/dev/null 2>&1 || { echo "adb not found" >&2; exit 1; }
count="$(adb devices | awk 'NR>1 && $2=="device"{n++} END{print n+0}')"
[[ "$count" -eq 1 ]] || { echo "Connect exactly one authorized ADB device (found $count)" >&2; exit 1; }

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
out="$root/backups/$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$out"
chmod 700 "$out"
adb shell pm list packages > "$out/packages.txt"
adb shell pm list packages -d > "$out/disabled-packages.txt"
adb shell getprop > "$out/getprop.txt"
for namespace in system secure global; do
  adb shell settings list "$namespace" > "$out/settings-$namespace.txt"
done
echo "Read-only device inventory saved locally: $out"
echo "Do not commit or share this directory; it may contain device identifiers."
