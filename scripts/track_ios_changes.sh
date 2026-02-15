#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IOS_REPO="${IOS_REPO:-$ROOT/../eof-ios}"
STATE_FILE="$ROOT/docs/IOS_LAST_SYNC.txt"

if [[ ! -d "$IOS_REPO/.git" ]]; then
  echo "Missing iOS repo at: $IOS_REPO" >&2
  exit 1
fi

current_head="$(git -C "$IOS_REPO" rev-parse HEAD)"
current_subject="$(git -C "$IOS_REPO" log -1 --pretty=%s)"
current_date="$(git -C "$IOS_REPO" log -1 --date=short --pretty=%cd)"

if [[ ! -f "$STATE_FILE" ]]; then
  {
    echo "# Last synced eof-ios commit"
    echo "$current_head"
  } > "$STATE_FILE"
  echo "Initialized $STATE_FILE"
  echo "Current eof-ios HEAD: $current_head ($current_date: $current_subject)"
  exit 0
fi

last_synced="$(tail -n 1 "$STATE_FILE" | tr -d '[:space:]')"

if [[ -z "$last_synced" ]]; then
  echo "State file is empty: $STATE_FILE" >&2
  exit 1
fi

if [[ "$last_synced" == "$current_head" ]]; then
  echo "No new eof-ios commits since last sync."
  echo "Tracked commit: $current_head"
  exit 0
fi

echo "eof-ios changed since last sync"
echo "Last synced: $last_synced"
echo "Current    : $current_head ($current_date: $current_subject)"
echo
echo "Commits since last sync:"
git -C "$IOS_REPO" log --oneline "$last_synced..$current_head"
echo
echo "Files changed since last sync:"
git -C "$IOS_REPO" diff --name-only "$last_synced..$current_head"

echo
echo "When Android parity updates are complete, record new sync point with:"
echo "  $0 --mark-synced"

if [[ "${1:-}" == "--mark-synced" ]]; then
  {
    echo "# Last synced eof-ios commit"
    echo "$current_head"
  } > "$STATE_FILE"
  echo "Updated $STATE_FILE -> $current_head"
fi
