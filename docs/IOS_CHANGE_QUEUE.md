# iOS Change Queue

Tracks `eof-ios` changes detected by `scripts/track_ios_changes.sh` that still need Android parity work.

## Pending

### dbb4c970ca93a2c339a7a3c57afd832da61c7b95 (2026-02-15)
- Commit: `Improve spectral plot with median, envelope, and dynamic Y scale`
- Files changed in `eof-ios`:
  - `eof/Models/AppSettings.swift`
  - `eof/Views/ContentView.swift`
  - `eof/Views/SettingsView.swift`
- Android parity status: pending
- Planned Android follow-up:
  - Add spectral plot median + envelope rendering support in Android UI chart components.
  - Add settings toggles/ranges for dynamic Y scaling behavior.
  - Add regression tests for plot scaling and summary statistics.

## Cleared

- None yet.
