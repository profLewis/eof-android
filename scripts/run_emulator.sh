#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

AVD_NAME="${AVD_NAME:-eofApi35}"
API_LEVEL="${API_LEVEL:-35}"
ABI="${ABI:-arm64-v8a}"
TAG="${TAG:-google_apis}"
DEVICE="${DEVICE:-pixel_7}"
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
WAIT="${WAIT:-0}"
LOGCAT="${LOGCAT:-0}"

export ANDROID_SDK_ROOT="$SDK_ROOT"

SDKMANAGER="${SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager"
AVDMANAGER="${SDK_ROOT}/cmdline-tools/latest/bin/avdmanager"
EMULATOR_BIN="${SDK_ROOT}/emulator/emulator"
ADB_BIN="${SDK_ROOT}/platform-tools/adb"

require_file() {
  local f="$1"
  local name="$2"
  if [[ ! -x "$f" ]]; then
    echo "Missing ${name}: ${f}" >&2
    echo "Install SDK first with: ./scripts/install_android_sdk.sh" >&2
    exit 1
  fi
}

require_file "$SDKMANAGER" "sdkmanager"
require_file "$AVDMANAGER" "avdmanager"
require_file "$ADB_BIN" "adb"

if [[ ! -x "$EMULATOR_BIN" ]]; then
  echo "Installing emulator package..."
  yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" "emulator"
fi
require_file "$EMULATOR_BIN" "emulator"

SYSIMG="system-images;android-${API_LEVEL};${TAG};${ABI}"
if ! "$SDKMANAGER" --sdk_root="$SDK_ROOT" --list_installed | rg -q "${SYSIMG}"; then
  echo "Installing emulator system image: ${SYSIMG}"
  yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" "$SYSIMG"
fi

if ! "$AVDMANAGER" list avd | rg -q "Name: ${AVD_NAME}$"; then
  echo "Creating AVD: ${AVD_NAME} (${DEVICE}, ${SYSIMG})"
  echo "no" | "$AVDMANAGER" create avd -n "$AVD_NAME" -k "$SYSIMG" -d "$DEVICE"
fi

if ! "$ADB_BIN" devices | rg -q "emulator-.*device"; then
  echo "Starting emulator: ${AVD_NAME}"
  nohup "$EMULATOR_BIN" -avd "$AVD_NAME" -no-snapshot-save -netdelay none -netspeed full >/tmp/eof-android-emulator.log 2>&1 &
fi

echo "Waiting for emulator to boot..."
"$ADB_BIN" wait-for-device
until [[ "$("$ADB_BIN" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
  sleep 2
done

"$ADB_BIN" shell input keyevent 82 >/dev/null 2>&1 || true

echo "Building and installing debug app..."
./gradlew installDebug

echo "Launching app..."
"$ADB_BIN" shell am start -n uk.ac.ucl.eof.android/.MainActivity

echo "Done. Emulator log: /tmp/eof-android-emulator.log"

if [[ "$LOGCAT" == "1" ]]; then
  echo "Streaming app logs (Ctrl+C to stop)..."
  "$ADB_BIN" logcat -s EOFAndroid ActivityManager AndroidRuntime
fi

if [[ "$WAIT" == "1" ]]; then
  echo "Emulator launched. Press Ctrl+C to exit this script."
  while true; do
    sleep 3600
  done
fi
