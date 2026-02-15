#!/usr/bin/env bash
set -euo pipefail

fail=0

check_cmd() {
  local cmd="$1"
  local label="$2"
  if command -v "$cmd" >/dev/null 2>&1; then
    echo "[ok] ${label}"
  else
    echo "[missing] ${label}"
    fail=1
  fi
}

check_cmd git "git"
check_cmd gh "GitHub CLI (gh)"

if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
  echo "[ok] Java 17 available"
else
  echo "[missing] Java 17"
  fail=1
fi

if [[ -d "$HOME/Library/Android/sdk" ]]; then
  echo "[ok] Android SDK directory exists"
else
  echo "[missing] Android SDK directory ($HOME/Library/Android/sdk)"
  fail=1
fi

if gh auth status >/dev/null 2>&1; then
  echo "[ok] gh auth valid"
else
  echo "[missing] gh auth valid (run: gh auth login -h github.com)"
  fail=1
fi

if [[ "$fail" -eq 0 ]]; then
  echo "All requirements satisfied."
else
  echo "Some requirements are missing. See REQUIREMENTS.md"
  exit 1
fi
