# eof-android

Android counterpart to `../eof-ios` for Earth Observation Fetch workflows.

## Goal
Keep Android functionally aligned with iOS capabilities while iOS continues evolving.

Primary reference: `../eof-ios/README.md`.

## What is implemented now
- Compose app scaffold with four feature tabs: Fetch, Sources, Phenology, Settings.
- AOI inputs (lat/lon + GeoJSON URL field).
- Multi-source configuration model (AWS, Planetary, CDSE, Earthdata, GEE).
- Synthetic EO pipeline for immediate runnable behavior.
- NDVI math and source comparison metrics (bias, RMSE, R2).
- Baseline double-logistic phenology fitting.
- Unit tests for math, services, and repository flow.
- iOS parity documentation:
  - `docs/CAPABILITY_MATRIX.md`
  - `docs/IOS_SYNC.md`
  - `docs/TESTING.md`

## Project layout
- `app/src/main/java/uk/ac/ucl/eof/android/model`: shared domain models
- `app/src/main/java/uk/ac/ucl/eof/android/service`: computation/data service layer
- `app/src/main/java/uk/ac/ucl/eof/android/repository`: app orchestration and state
- `app/src/main/java/uk/ac/ucl/eof/android/ui`: Compose UI + view model
- `app/src/test/java/uk/ac/ucl/eof/android`: unit tests
- `scripts/check_ios_capability_drift.sh`: sync helper against iOS README

## How to run
1. Open `eof-android` in Android Studio (latest stable).
2. Let Gradle sync complete.
3. Run on emulator (API 26+) or USB-connected Android device.

Command line (if SDK/Java are configured):
```bash
./gradlew test
./gradlew installDebug
```

Environment prerequisites and auto-push setup are documented in `REQUIREMENTS.md`.

## How to test
Automated:
```bash
./gradlew test
```

Manual workflow:
1. Open app -> `Sources`: enable at least two sources with pixel support.
2. `Fetch`: set AOI and tap `Fetch`.
3. Tap `Compare Sources` and verify metrics render.
4. `Phenology`: tap `Fit Phenology`, verify params + pixel summary.
5. `Settings`: change parameters and repeat fit.

Detailed plan: `docs/TESTING.md`.

## Keeping parity with iOS
For each iOS change:
1. Run `scripts/check_ios_capability_drift.sh`.
2. Update `docs/CAPABILITY_MATRIX.md`.
3. Add tests for changed behavior.
4. Implement Android updates.
5. Run `./gradlew test`.

## Quick machine check
Run:
```bash
./scripts/check_requirements.sh
```

## Next engineering steps
1. Replace synthetic fetch with real STAC + COG readers.
2. Add token managers (CDSE, Earthdata, GEE) and encrypted credential storage.
3. Upgrade phenology fitter to match iOS algorithm details (Nelder-Mead + Huber + ensemble restarts).
4. Add per-band comparison plots and per-date diagnostics table.
5. Add instrumentation UI tests and golden-scene regression tests.
