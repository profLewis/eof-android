#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IOS_REPO="${IOS_REPO:-$ROOT/../eof-ios}"
STATE_FILE="$ROOT/docs/IOS_LAST_SYNC.txt"
QUEUE_FILE="$ROOT/docs/IOS_CHANGE_QUEUE.md"
STATUS_FILE="$ROOT/docs/IOS_TRACK_STATUS.md"
NOW_UTC="$(date -u +"%Y-%m-%d %H:%M UTC")"

if [[ ! -d "$IOS_REPO/.git" ]]; then
  echo "Missing iOS repo at: $IOS_REPO" >&2
  exit 1
fi

write_status() {
  local last_synced="$1"
  local current_head="$2"
  local current_date="$3"
  local current_subject="$4"
  local pending_count="$5"

  cat > "$STATUS_FILE" <<STATUS
# iOS Tracking Status

- Updated: $NOW_UTC
- Last synced iOS commit: \

a$last_synced
- Current iOS HEAD: \

a$current_head
- Current iOS HEAD summary: $current_date - $current_subject
- Pending iOS commits to mirror on Android: $pending_count

Use \`./scripts/track_ios_changes.sh\` for terminal details.
Use \`./scripts/track_ios_changes.sh --mark-synced\` after parity updates are complete.
STATUS
}

write_queue_none() {
  local last_synced="$1"
  local current_head="$2"
  cat > "$QUEUE_FILE" <<QUEUE
# iOS Change Queue

Tracks \`eof-ios\` changes detected by \`scripts/track_ios_changes.sh\` that still need Android parity work.

## Status
- Generated: $NOW_UTC
- Last synced iOS commit: \

a$last_synced
- Current iOS HEAD: \

a$current_head
- Pending commits: 0

## Pending
- None.

## Cleared
- Managed via sync checkpoints in \`docs/IOS_LAST_SYNC.txt\`.
QUEUE
}

write_queue_pending() {
  local last_synced="$1"
  local current_head="$2"
  local commit_lines="$3"
  local file_lines="$4"

  cat > "$QUEUE_FILE" <<QUEUE
# iOS Change Queue

Tracks \`eof-ios\` changes detected by \`scripts/track_ios_changes.sh\` that still need Android parity work.

## Status
- Generated: $NOW_UTC
- Last synced iOS commit: \

a$last_synced
- Current iOS HEAD: \

a$current_head

## Pending iOS commits
$commit_lines

## Changed files since last sync
$file_lines

## Android action checklist
- Update \`docs/CAPABILITY_MATRIX.md\` for impacted capabilities.
- Add/adjust Android tests before implementation.
- Implement parity changes.
- Run \`./gradlew test\`.
- Mark synced when complete: \`./scripts/track_ios_changes.sh --mark-synced\`.
QUEUE
}

current_head="$(git -C "$IOS_REPO" rev-parse HEAD)"
current_subject="$(git -C "$IOS_REPO" log -1 --pretty=%s)"
current_date="$(git -C "$IOS_REPO" log -1 --date=short --pretty=%cd)"

if [[ ! -f "$STATE_FILE" ]]; then
  {
    echo "# Last synced eof-ios commit"
    echo "$current_head"
  } > "$STATE_FILE"
  write_status "$current_head" "$current_head" "$current_date" "$current_subject" 0
  write_queue_none "$current_head" "$current_head"
  echo "Initialized $STATE_FILE"
  echo "Current eof-ios HEAD: $current_head ($current_date: $current_subject)"
  exit 0
fi

last_synced="$(tail -n 1 "$STATE_FILE" | tr -d '[:space:]')"

if [[ -z "$last_synced" ]]; then
  echo "State file is empty: $STATE_FILE" >&2
  exit 1
fi

if [[ "${1:-}" == "--mark-synced" ]]; then
  {
    echo "# Last synced eof-ios commit"
    echo "$current_head"
  } > "$STATE_FILE"
  write_status "$current_head" "$current_head" "$current_date" "$current_subject" 0
  write_queue_none "$current_head" "$current_head"
  echo "Updated $STATE_FILE -> $current_head"
  exit 0
fi

if [[ "$last_synced" == "$current_head" ]]; then
  write_status "$last_synced" "$current_head" "$current_date" "$current_subject" 0
  write_queue_none "$last_synced" "$current_head"
  echo "No new eof-ios commits since last sync."
  echo "Tracked commit: $current_head"
  exit 0
fi

commit_lines="$(git -C "$IOS_REPO" log --date=short --pretty=format:'- `%h` %ad %s' "$last_synced..$current_head")"
file_lines="$(git -C "$IOS_REPO" diff --name-only "$last_synced..$current_head" | sed 's#^#- `#; s#$#`#')"
pending_count="$(git -C "$IOS_REPO" rev-list --count "$last_synced..$current_head")"

write_status "$last_synced" "$current_head" "$current_date" "$current_subject" "$pending_count"
write_queue_pending "$last_synced" "$current_head" "$commit_lines" "$file_lines"

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
echo "Auto-updated: $STATUS_FILE"
echo "Auto-updated: $QUEUE_FILE"
echo "When Android parity updates are complete, run:"
echo "  $0 --mark-synced"
