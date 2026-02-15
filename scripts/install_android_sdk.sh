#!/usr/bin/env bash
set -euo pipefail

SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
mkdir -p "$SDK_ROOT"

find_sdkmanager() {
  if command -v sdkmanager >/dev/null 2>&1; then
    command -v sdkmanager
    return 0
  fi

  local candidates=(
    "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
    "$HOME/Library/Android/sdk/cmdline-tools/latest/bin/sdkmanager"
    "/opt/homebrew/share/android-commandlinetools/cmdline-tools/bin/sdkmanager"
    "/usr/local/share/android-commandlinetools/cmdline-tools/bin/sdkmanager"
    "/Applications/Android Studio.app/Contents/bin/sdkmanager"
  )

  for c in "${candidates[@]}"; do
    if [[ -x "$c" ]]; then
      echo "$c"
      return 0
    fi
  done

  return 1
}

ensure_cmdline_tools() {
  if find_sdkmanager >/dev/null 2>&1; then
    return 0
  fi

  if ! command -v brew >/dev/null 2>&1; then
    echo "Homebrew is required to auto-install Android command-line tools." >&2
    echo "Install Homebrew first: https://brew.sh" >&2
    exit 1
  fi

  echo "Installing Android command-line tools via Homebrew..."
  brew install --cask android-commandlinetools
}

ensure_cmdline_tools
SDKMANAGER="$(find_sdkmanager)"

export ANDROID_SDK_ROOT="$SDK_ROOT"

echo "Using sdkmanager: $SDKMANAGER"
echo "Using ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"

# Ensure sdkmanager can run before install.
"$SDKMANAGER" --version

# Install baseline packages for this project.
yes | "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null || true
"$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0" \
  "cmdline-tools;latest"

echo
echo "Android SDK installed at: $ANDROID_SDK_ROOT"
echo "Add to your shell profile (e.g. ~/.zshrc):"
echo "  export ANDROID_SDK_ROOT=\"$ANDROID_SDK_ROOT\""
echo "  export PATH=\"$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$PATH\""
