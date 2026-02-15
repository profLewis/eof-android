#!/usr/bin/env bash
set -euo pipefail

fail=0
fixes=()

check_cmd() {
  local cmd="$1"
  local label="$2"
  local fix="$3"
  if command -v "$cmd" >/dev/null 2>&1; then
    echo "[ok] ${label}"
  else
    echo "[missing] ${label}"
    fixes+=("$fix")
    fail=1
  fi
}

check_cmd git "git" "Install git (Xcode CLT): xcode-select --install"
check_cmd gh "GitHub CLI (gh)" "Install gh: brew install gh"

if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
  echo "[ok] Java 17 available"
else
  echo "[missing] Java 17"
  fixes+=("Install Java 17: brew install --cask temurin@17")
  fail=1
fi

if [[ -d "$HOME/Library/Android/sdk" ]]; then
  echo "[ok] Android SDK directory exists"
else
  echo "[missing] Android SDK directory ($HOME/Library/Android/sdk)"
  fixes+=("Install Android SDK: ./scripts/install_android_sdk.sh")
  fail=1
fi

if gh auth status >/dev/null 2>&1; then
  echo "[ok] gh auth valid"
else
  echo "[missing] gh auth valid"
  fixes+=("Authenticate gh: gh auth login -h github.com")
  fail=1
fi

if [[ -x "$HOME/Library/Android/sdk/emulator/emulator" ]]; then
  echo "[ok] Android emulator binary exists"
else
  echo "[missing] Android emulator binary"
  fixes+=("Install emulator tools: ./scripts/install_android_sdk.sh")
  fail=1
fi

if [[ -f "local.properties" ]] && rg -q "^sdk.dir=" "local.properties"; then
  echo "[ok] local.properties has sdk.dir"
else
  echo "[missing] local.properties sdk.dir"
  fixes+=("Create local.properties: printf 'sdk.dir=%s\\n' \"$HOME/Library/Android/sdk\" > local.properties")
  fail=1
fi

if [[ "$fail" -eq 0 ]]; then
  echo "All requirements satisfied."
else
  echo
  echo "Fix commands:"
  for fix in "${fixes[@]}"; do
    echo " - $fix"
  done
  echo
  echo "See REQUIREMENTS.md for full setup details."
  exit 1
fi
