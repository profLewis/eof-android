#!/usr/bin/env bash
set -euo pipefail

git config core.hooksPath .githooks
git config hooks.autopush true

echo "Auto-push enabled for this repo."
echo "Disable with: git config hooks.autopush false"
