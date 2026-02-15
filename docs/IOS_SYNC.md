# iOS Sync Contract

This Android repo is expected to stay derived from `../eof-ios`.

## Source of Truth
- Product behavior and capability definitions come from `../eof-ios/README.md` and iOS service/view implementations.
- Android architecture should preserve equivalent module boundaries:
  - iOS `Models` -> Android `model`
  - iOS `Services` -> Android `service`
  - iOS view orchestration -> Android `repository` + `ui`

## Update Workflow (every iOS feature change)
1. Pull latest iOS changes in `../eof-ios`.
2. Run `scripts/track_ios_changes.sh` to detect new commits and changed files since last sync.
3. Run `scripts/check_ios_capability_drift.sh`.
4. Update `docs/CAPABILITY_MATRIX.md` and `docs/IOS_CHANGE_QUEUE.md` for each impacted capability/change.
5. Add or update Android tests for any changed behavior.
6. Implement Android code changes.
7. Re-run tests (`./gradlew test`).
8. Record sync checkpoint: `scripts/track_ios_changes.sh --mark-synced`.
9. Commit with message prefix `sync(ios): ...`.

## Stability Rules
- Keep service interfaces stable so source adapters can be swapped without UI rewrites.
- Land behavior with tests before wiring production data backends.
- Do not remove a capability from Android unless it is explicitly removed from iOS.
