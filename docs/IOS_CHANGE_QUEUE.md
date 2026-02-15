# iOS Change Queue

Tracks `eof-ios` changes detected by `scripts/track_ios_changes.sh` that still need Android parity work.

## Status
- Generated: 2026-02-15 18:35 UTC
- Last synced iOS commit: 
ac1b5d813ad9674bca9125297eb1b98701a537bc2
- Current iOS HEAD: 
ae463b913f3bbd1b7e56f8e85494073f760c182be

## Pending iOS commits
- `e463b91` 2026-02-15 Add Unmix button, auto re-fit on dlFitTarget change, show fractions on chart
- `7bced5a` 2026-02-15 Center movie frame vertically and horizontally in container
- `dbb4c97` 2026-02-15 Improve spectral plot with median, envelope, and dynamic Y scale

## Changed files since last sync
- `eof/Models/AppSettings.swift`
- `eof/Views/ContentView.swift`
- `eof/Views/SettingsView.swift`

## Android action checklist
- Update `docs/CAPABILITY_MATRIX.md` for impacted capabilities.
- Add/adjust Android tests before implementation.
- Implement parity changes.
- Run `./gradlew test`.
- Mark synced when complete: `./scripts/track_ios_changes.sh --mark-synced`.
