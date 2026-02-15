# Architecture

`eof-android` is structured to mirror iOS modules while staying easy to evolve.

## Package layout

- `model`: domain state and typed config/data classes.
- `service`: computation and data backends.
- `repository`: state orchestration and use-case flows.
- `ui`: Compose screens and `MainViewModel`.
- `util`: math helpers.

## Runtime flow

1. UI dispatches action via `MainViewModel`.
2. `MainViewModel` delegates to `EORepository`.
3. `EORepository` calls service methods and updates `StateFlow<AppState>`.
4. Compose UI reacts to state changes.

## Data and method boundaries

- `SyntheticEOService` is a placeholder backend.
- `PhenologyService` is independent from data source I/O.
- `NdviMath` is isolated for direct test coverage.

## Extension strategy for iOS parity

When replacing synthetic data with real backends:
1. Keep repository/public state contracts stable.
2. Swap service implementations under same call sites.
3. Add tests for new behavior before connecting UI-specific logic.

## Key files

- App state model: `app/src/main/java/uk/ac/ucl/eof/android/model/Models.kt`
- Repository orchestration: `app/src/main/java/uk/ac/ucl/eof/android/repository/EORepository.kt`
- Phenology method: `app/src/main/java/uk/ac/ucl/eof/android/service/PhenologyService.kt`
- Source compare method: `app/src/main/java/uk/ac/ucl/eof/android/service/SyntheticEOService.kt`
- UI root: `app/src/main/java/uk/ac/ucl/eof/android/ui/EofApp.kt`
