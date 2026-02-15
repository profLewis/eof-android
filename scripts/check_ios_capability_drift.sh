#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IOS_README="${ROOT}/../eof-ios/README.md"
MATRIX="${ROOT}/docs/CAPABILITY_MATRIX.md"

if [[ ! -f "${IOS_README}" ]]; then
  echo "Missing iOS README at ${IOS_README}" >&2
  exit 1
fi

if [[ ! -f "${MATRIX}" ]]; then
  echo "Missing Android capability matrix at ${MATRIX}" >&2
  exit 1
fi

echo "iOS capability bullets:"
awk '
  /^## What It Does/{in_section=1;next}
  /^## / && in_section{exit}
  in_section && /^- /{print}
' "${IOS_README}"

echo
echo "Android matrix entries:"
awk '/^\|/{print}' "${MATRIX}"

echo
echo "Manual review required: update docs/CAPABILITY_MATRIX.md when iOS capabilities change."
